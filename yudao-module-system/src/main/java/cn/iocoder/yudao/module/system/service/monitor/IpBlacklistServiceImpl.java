package cn.iocoder.yudao.module.system.service.monitor;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.IpBlacklistAddReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.IpBlacklistDO;
import cn.iocoder.yudao.module.system.dal.mysql.monitor.IpBlacklistMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class IpBlacklistServiceImpl implements IpBlacklistService {

    /** 每个 IP 独立的 Redis key，TTL = 剩余封禁时长 */
    private static final String IP_KEY          = "security:ip:ban:%s";
    /** 永久封禁使用 100 年作为占位 TTL（Redis key 必须有 TTL） */
    private static final long   PERMANENT_SECS  = 100L * 365 * 24 * 3600;
    /** 兼容旧版 Set key，在 refreshCache 时一并清除 */
    private static final String LEGACY_SET_KEY  = "security:ip:blacklist";

    @Resource
    private IpBlacklistMapper ipBlacklistMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @PostConstruct
    public void init() {
        try {
            refreshCache();
        } catch (Exception e) {
            log.warn("[IpBlacklist] 初始化缓存失败，将在首次查询时重建", e);
        }
    }

    @Override
    public boolean isBlacklisted(String ip) {
        if (ip == null) return false;
        // 先查 Redis 独立 key（O(1)，带 TTL 自动过期）
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(String.format(IP_KEY, ip)))) return true;
        // Redis 无记录时回查 DB（冷启动或 key 丢失）
        return ipBlacklistMapper.countActiveByIp(ip) > 0;
    }

    @Override
    public void addToBlacklist(String ip, String reason, boolean autoAdded, LocalDateTime expireTime) {
        // 1. 写库
        IpBlacklistDO existing = ipBlacklistMapper.selectOne(
                new LambdaQueryWrapper<IpBlacklistDO>().eq(IpBlacklistDO::getIp, ip));
        if (existing != null) {
            existing.setReason(reason);
            existing.setExpireTime(expireTime);
            ipBlacklistMapper.updateById(existing);
        } else {
            IpBlacklistDO record = new IpBlacklistDO();
            record.setIp(ip);
            record.setReason(reason);
            record.setAutoAdded(autoAdded);
            record.setExpireTime(expireTime);
            record.setCreateTime(LocalDateTime.now());
            ipBlacklistMapper.insert(record);
        }
        // 2. 写 Redis（独立 key + TTL 与过期时间对齐）
        setRedisKey(ip, expireTime);
        log.warn("[IpBlacklist] IP 已加入黑名单: {} 原因: {} 过期: {}", ip, reason,
                expireTime != null ? expireTime : "永久");
    }

    @Override
    public void addToBlacklist(IpBlacklistAddReqVO reqVO) {
        addToBlacklist(reqVO.getIp(), reqVO.getReason(), false, reqVO.getExpireTime());
    }

    @Override
    public void removeFromBlacklist(Long id) {
        IpBlacklistDO record = ipBlacklistMapper.selectById(id);
        if (record != null) {
            ipBlacklistMapper.deleteById(id);
            stringRedisTemplate.delete(String.format(IP_KEY, record.getIp()));
        }
    }

    @Override
    public PageResult<IpBlacklistDO> getPage(PageParam pageParam) {
        return ipBlacklistMapper.selectPage(pageParam, new LambdaQueryWrapperX<IpBlacklistDO>()
                .orderByDesc(IpBlacklistDO::getId));
    }

    @Override
    public void refreshCache() {
        // 清除旧版 Set key
        stringRedisTemplate.delete(LEGACY_SET_KEY);

        // 查所有有效记录，重建各自的独立 key
        List<IpBlacklistDO> actives = ipBlacklistMapper.selectList(
                new LambdaQueryWrapper<IpBlacklistDO>()
                        .and(w -> w.isNull(IpBlacklistDO::getExpireTime)
                                   .or().gt(IpBlacklistDO::getExpireTime, LocalDateTime.now())));
        for (IpBlacklistDO record : actives) {
            setRedisKey(record.getIp(), record.getExpireTime());
        }
        log.info("[IpBlacklist] 黑名单缓存已刷新，共 {} 个IP", actives.size());
    }

    // ── 工具方法 ──────────────────────────────────────────────────

    private void setRedisKey(String ip, LocalDateTime expireTime) {
        String key = String.format(IP_KEY, ip);
        if (expireTime != null) {
            long ttl = Duration.between(LocalDateTime.now(), expireTime).getSeconds();
            if (ttl > 0) {
                stringRedisTemplate.opsForValue().set(key, "1", ttl, TimeUnit.SECONDS);
            }
            // ttl <= 0 说明已过期，不写入（等价于未封禁）
        } else {
            stringRedisTemplate.opsForValue().set(key, "1", PERMANENT_SECS, TimeUnit.SECONDS);
        }
    }
}
