package cn.iocoder.yudao.module.system.service.monitor;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.IpBlacklistAddReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.IpBlacklistDO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.IpBlacklistLogDO;
import cn.iocoder.yudao.module.system.dal.mysql.monitor.IpBlacklistLogMapper;
import cn.iocoder.yudao.module.system.dal.mysql.monitor.IpBlacklistMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class IpBlacklistServiceImpl implements IpBlacklistService {

    private static final String            IP_KEY         = "security:ip:ban:%s";
    private static final long              PERMANENT_SECS = 100L * 365 * 24 * 3600;
    private static final DateTimeFormatter EXPIRE_FMT     = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String            LEGACY_SET_KEY = "security:ip:blacklist";

    @Resource private IpBlacklistMapper    ipBlacklistMapper;
    @Resource private IpBlacklistLogMapper ipBlacklistLogMapper;
    @Resource private StringRedisTemplate  stringRedisTemplate;
    @Resource private IpWhitelistService   ipWhitelistService;

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
        if (ipWhitelistService.isWhitelisted(ip)) return false; // 白名单 IP 永不视为黑名单
        // 只查 Redis：启动 + 定时 refreshCache 从库重建，增删黑名单实时维护 Redis key；
        // 不再每请求回查数据库，避免正常流量对 DB 造成"每请求一次查询"的压力（SecurityDetectFilter 每请求都会调）。
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(String.format(IP_KEY, ip)));
    }

    /** 定时从库重建 Redis 缓存：兜底 Redis 重启/驱逐导致的丢失（默认5分钟一次） */
    @org.springframework.scheduling.annotation.Scheduled(fixedDelayString = "${yudao.ip-blacklist.refresh-ms:300000}")
    public void scheduledRefresh() {
        try {
            refreshCache();
        } catch (Exception e) {
            log.warn("[IpBlacklist] 定时刷新缓存失败：{}", e.getMessage());
        }
    }

    @Override
    public void addToBlacklist(String ip, String reason, boolean autoAdded, LocalDateTime expireTime) {
        // 白名单 IP 躲过所有封禁
        if (ipWhitelistService.isWhitelisted(ip)) {
            log.info("[IpBlacklist] IP {} 在白名单，跳过封禁（原因: {}）", ip, reason);
            return;
        }
        IpBlacklistDO existing = ipBlacklistMapper.selectOne(
                new LambdaQueryWrapper<IpBlacklistDO>().eq(IpBlacklistDO::getIp, ip));

        if (existing != null) {
            ipBlacklistMapper.update(null, new LambdaUpdateWrapper<IpBlacklistDO>()
                    .eq(IpBlacklistDO::getId, existing.getId())
                    .set(IpBlacklistDO::getReason, reason)
                    .set(IpBlacklistDO::getExpireTime, expireTime)
                    .set(IpBlacklistDO::getStatus, 0)
                    .setSql("ban_count = ban_count + 1"));
        } else {
            IpBlacklistDO record = new IpBlacklistDO();
            record.setIp(ip);
            record.setReason(reason);
            record.setAutoAdded(autoAdded);
            record.setExpireTime(expireTime);
            record.setBanCount(1);
            record.setStatus(0);
            record.setCreateTime(LocalDateTime.now());
            ipBlacklistMapper.insert(record);
        }

        // 写历史日志
        IpBlacklistLogDO entry = new IpBlacklistLogDO();
        entry.setIp(ip);
        entry.setReason(reason);
        entry.setAutoAdded(autoAdded);
        entry.setExpireTime(expireTime);
        entry.setCreateTime(LocalDateTime.now());
        ipBlacklistLogMapper.insert(entry);

        setRedisKey(ip, expireTime);
        log.warn("[IpBlacklist] IP 已加入黑名单: {} 原因: {} 过期: {}", ip, reason,
                expireTime != null ? expireTime : "永久");
    }

    @Override
    public void addToBlacklist(IpBlacklistAddReqVO reqVO) {
        LocalDateTime expireTime = null;
        if (reqVO.getExpireTime() != null && !reqVO.getExpireTime().trim().isEmpty()) {
            expireTime = LocalDateTime.parse(reqVO.getExpireTime().trim(), EXPIRE_FMT);
        }
        addToBlacklist(reqVO.getIp(), reqVO.getReason(), false, expireTime);
    }

    @Override
    public void removeFromBlacklist(Long id) {
        IpBlacklistDO record = ipBlacklistMapper.selectById(id);
        if (record == null) return;
        ipBlacklistMapper.update(null, new LambdaUpdateWrapper<IpBlacklistDO>()
                .eq(IpBlacklistDO::getId, id)
                .set(IpBlacklistDO::getStatus, 1));
        stringRedisTemplate.delete(String.format(IP_KEY, record.getIp()));
        log.info("[IpBlacklist] IP 已解封: {}", record.getIp());
    }

    @Override
    public PageResult<IpBlacklistDO> getPage(PageParam pageParam) {
        return ipBlacklistMapper.selectPage(pageParam, new LambdaQueryWrapperX<IpBlacklistDO>()
                .eq(IpBlacklistDO::getStatus, 0)
                .orderByDesc(IpBlacklistDO::getUpdateTime)
                .orderByDesc(IpBlacklistDO::getId));
    }

    @Override
    public List<IpBlacklistLogDO> getBanLogs(String ip) {
        return ipBlacklistLogMapper.selectByIp(ip);
    }

    @Override
    public void refreshCache() {
        stringRedisTemplate.delete(LEGACY_SET_KEY);
        List<IpBlacklistDO> actives = ipBlacklistMapper.selectList(
                new LambdaQueryWrapper<IpBlacklistDO>()
                        .eq(IpBlacklistDO::getStatus, 0)
                        .and(w -> w.isNull(IpBlacklistDO::getExpireTime)
                                   .or().gt(IpBlacklistDO::getExpireTime, LocalDateTime.now())));
        for (IpBlacklistDO record : actives) {
            setRedisKey(record.getIp(), record.getExpireTime());
        }
        log.info("[IpBlacklist] 黑名单缓存已刷新，共 {} 个IP", actives.size());
    }

    private void setRedisKey(String ip, LocalDateTime expireTime) {
        String key = String.format(IP_KEY, ip);
        if (expireTime != null) {
            long ttl = Duration.between(LocalDateTime.now(), expireTime).getSeconds();
            if (ttl > 0) {
                stringRedisTemplate.opsForValue().set(key, "1", ttl, TimeUnit.SECONDS);
            }
        } else {
            stringRedisTemplate.opsForValue().set(key, "1", PERMANENT_SECS, TimeUnit.SECONDS);
        }
    }
}
