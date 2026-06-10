package cn.iocoder.yudao.module.system.service.monitor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.DataAccessConfigPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.DataAccessConfigSaveReqVO;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.DataAccessLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.DataAccessConfigDO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.DataAccessLogDO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.SecurityAlertDO;
import cn.iocoder.yudao.module.system.dal.mysql.monitor.DataAccessConfigMapper;
import cn.iocoder.yudao.module.system.dal.mysql.monitor.DataAccessLogMapper;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DataAccessMonitorServiceImpl implements DataAccessMonitorService {

    /** Redis 配置缓存 key：data_access:config:{module}:{entityType} */
    private static final String CONFIG_CACHE_KEY = "data_access:config:%s:%s";
    /** Redis 访问计数 key：data_access:count:{module}:{entityType}:{userId}:{windowStart} */
    private static final String COUNT_KEY = "data_access:count:%s:%s:%d:%d";
    private static final long CONFIG_CACHE_TTL = 300; // 5 分钟

    @Resource
    private DataAccessConfigMapper dataAccessConfigMapper;
    @Resource
    private DataAccessLogMapper dataAccessLogMapper;
    @Resource
    private SecurityAlertService securityAlertService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Async
    public void recordAccess(Long userId, String username, String module, String entityType,
                              String queryParams, List<Long> resultIds, int resultCount,
                              String requestUrl, String clientIp) {
        // 1. 写访问日志
        DataAccessLogDO log = new DataAccessLogDO();
        log.setUserId(userId);
        log.setUsername(username);
        log.setModule(module);
        log.setEntityType(entityType);
        log.setQueryParams(queryParams);
        log.setResultIds(resultIds != null ? JSON.toJSONString(resultIds) : "[]");
        log.setResultCount(resultCount);
        log.setRequestUrl(requestUrl);
        log.setClientIp(clientIp);
        log.setAccessTime(LocalDateTime.now());
        log.setDeleted(0);
        try {
            dataAccessLogMapper.insert(log);
        } catch (Exception e) {
            this.log.error("[DataAccessMonitor] 日志写入失败 userId={}", userId, e);
        }

        if (resultCount <= 0 || userId == null) return;

        // 2. 找到适用的配置（先找用户级，再找全局默认）
        DataAccessConfigDO config = findConfig(module, entityType, userId);
        if (config == null || !Boolean.TRUE.equals(config.getEnabled())) return;
        if (config.getMaxRecordsPerWindow() == null) return; // 不限制

        // 3. Redis 计数（固定时间窗口）
        int windowSec = config.getWindowSeconds() != null ? config.getWindowSeconds() : 86400;
        long windowMs = (long) windowSec * 1000;
        long windowStart = (System.currentTimeMillis() / windowMs) * windowMs;
        String countKey = String.format(COUNT_KEY, module, entityType, userId, windowStart);

        Long total = stringRedisTemplate.opsForValue().increment(countKey, resultCount);
        if (total != null && total == resultCount) {
            // 首次写入，设置 TTL
            stringRedisTemplate.expire(countKey, windowSec * 2L, TimeUnit.SECONDS);
        }

        // 4. 超限告警（只告警，不拦截）
        if (total != null && total >= config.getMaxRecordsPerWindow()) {
            String msg = String.format(
                    "用户[%s(%d)] 在 %d 秒窗口内累计访问 [%s/%s] 数据 %d 条，超过上限 %d 条",
                    username, userId, windowSec, module, entityType, total, config.getMaxRecordsPerWindow());
            this.log.warn("[DataAccessMonitor] {}", msg);
            SecurityAlertDO alert = new SecurityAlertDO();
            alert.setAlertType("DATA_LEAKAGE");
            alert.setSeverity(2);
            alert.setUserId(userId);
            alert.setRequestUrl(requestUrl);
            alert.setRequestMethod("GET");
            alert.setAlertMessage(msg);
            alert.setDetail(String.format(
                    "{\"module\":\"%s\",\"entityType\":\"%s\",\"windowSeconds\":%d,\"total\":%d,\"limit\":%d}",
                    module, entityType, windowSec, total, config.getMaxRecordsPerWindow()));
            alert.setHandled(0);
            securityAlertService.saveAsync(alert);
        }
    }

    // ─── 配置管理 ────────────────────────────────────────────

    @Override
    public Long createConfig(DataAccessConfigSaveReqVO req) {
        DataAccessConfigDO config = toConfigDO(req);
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateTime(LocalDateTime.now());
        config.setDeleted(0);
        dataAccessConfigMapper.insert(config);
        evictConfigCache(req.getModule(), req.getEntityType());
        return config.getId();
    }

    @Override
    public void updateConfig(DataAccessConfigSaveReqVO req) {
        DataAccessConfigDO config = toConfigDO(req);
        config.setUpdateTime(LocalDateTime.now());
        dataAccessConfigMapper.updateById(config);
        evictConfigCache(req.getModule(), req.getEntityType());
    }

    @Override
    public void deleteConfig(Long id) {
        DataAccessConfigDO existing = dataAccessConfigMapper.selectById(id);
        if (existing != null) {
            existing.setDeleted(1);
            dataAccessConfigMapper.updateById(existing);
            evictConfigCache(existing.getModule(), existing.getEntityType());
        }
    }

    @Override
    public DataAccessConfigDO getConfig(Long id) {
        return dataAccessConfigMapper.selectById(id);
    }

    @Override
    public PageResult<DataAccessConfigDO> getConfigPage(DataAccessConfigPageReqVO req) {
        return dataAccessConfigMapper.selectPage(req);
    }

    // ─── 日志查询 ────────────────────────────────────────────

    @Override
    public PageResult<DataAccessLogDO> getLogPage(DataAccessLogPageReqVO req) {
        return dataAccessLogMapper.selectPage(req);
    }

    // ─── 内部工具 ────────────────────────────────────────────

    private DataAccessConfigDO findConfig(String module, String entityType, Long userId) {
        String cacheKey = String.format(CONFIG_CACHE_KEY, module, entityType);
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        List<DataAccessConfigDO> configs;
        if (cached != null) {
            configs = JSON.parseArray(cached, DataAccessConfigDO.class);
        } else {
            configs = dataAccessConfigMapper.selectByModuleAndEntityType(module, entityType);
            stringRedisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(configs),
                    CONFIG_CACHE_TTL, TimeUnit.SECONDS);
        }
        // 先找用户级配置
        for (DataAccessConfigDO c : configs) {
            if (userId.equals(c.getUserId())) return c;
        }
        // 再找全局默认
        for (DataAccessConfigDO c : configs) {
            if (c.getUserId() == null) return c;
        }
        return null;
    }

    private void evictConfigCache(String module, String entityType) {
        stringRedisTemplate.delete(String.format(CONFIG_CACHE_KEY, module, entityType));
    }

    private DataAccessConfigDO toConfigDO(DataAccessConfigSaveReqVO req) {
        DataAccessConfigDO c = new DataAccessConfigDO();
        c.setId(req.getId());
        c.setModule(req.getModule());
        c.setEntityType(req.getEntityType());
        c.setUserId(req.getUserId());
        c.setEnabled(req.getEnabled());
        c.setMaxRecordsPerWindow(req.getMaxRecordsPerWindow());
        c.setWindowSeconds(req.getWindowSeconds() != null ? req.getWindowSeconds() : 86400);
        c.setRemark(req.getRemark());
        return c;
    }
}
