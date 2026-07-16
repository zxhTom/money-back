package cn.iocoder.yudao.module.system.service.monitor;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.IpAccessLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.IpAccessLogDO;
import cn.iocoder.yudao.module.system.dal.mysql.monitor.IpAccessLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * IP 访问尝试记录：记录被拦截/可疑的访问，并提供每日频率、TopIP、明细查询。
 */
@Service
@Slf4j
public class IpAccessLogService {

    @Resource
    private IpAccessLogMapper ipAccessLogMapper;
    @Resource
    private IpBlacklistService ipBlacklistService;

    // 内存聚合缓冲：热路径只做内存累加，由定时任务批量刷库，避免每请求一次 DB 写入。
    private final java.util.concurrent.ConcurrentHashMap<String, Agg> buffer =
            new java.util.concurrent.ConcurrentHashMap<>();
    private static final int MAX_BUFFER = 50_000;

    private static final class Agg {
        final String ip, reason;
        final LocalDateTime minute;
        final java.util.concurrent.atomic.AtomicLong count = new java.util.concurrent.atomic.AtomicLong();
        volatile String uri, method, userAgent;
        volatile Long userId;
        Agg(String ip, String reason, LocalDateTime minute) {
            this.ip = ip; this.reason = reason; this.minute = minute;
        }
    }

    /** 记录一次访问尝试（被拦截/可疑时调用）。只做内存累加，永不抛出、不碰数据库。 */
    public void record(String ip, String uri, String method, String userAgent, String reason, Long userId) {
        try {
            if (StrUtil.isBlank(ip)) {
                return;
            }
            if (buffer.size() >= MAX_BUFFER) {
                return; // 极端攻击下保护内存，丢弃（下次刷库后恢复）
            }
            LocalDateTime minute = LocalDateTime.now().withSecond(0).withNano(0);
            String key = ip + '|' + reason + '|' + minute;
            Agg a = buffer.computeIfAbsent(key, k -> new Agg(ip, reason, minute));
            a.count.incrementAndGet();
            a.uri = StrUtil.sub(uri, 0, 255);
            a.method = StrUtil.sub(method, 0, 16);
            a.userAgent = StrUtil.sub(userAgent, 0, 512);
            a.userId = userId;
        } catch (Exception e) {
            log.debug("[IpAccessLog] 记录失败，忽略：{}", e.getMessage());
        }
    }

    /** 定时把内存缓冲批量刷入数据库（默认30秒一次）。把"每请求一次写库"降为"每30秒一批"。 */
    @org.springframework.scheduling.annotation.Scheduled(fixedDelayString = "${yudao.ip-access-log.flush-ms:30000}")
    public void flush() {
        if (buffer.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (String key : new java.util.ArrayList<>(buffer.keySet())) {
            Agg a = buffer.remove(key);
            if (a == null) {
                continue;
            }
            long delta = a.count.get();
            if (delta <= 0) {
                continue;
            }
            try {
                ipAccessLogMapper.upsert(a.ip, a.reason, a.uri, a.method, a.userAgent, a.userId,
                        a.minute.toLocalDate(), a.minute, now, delta);
            } catch (Exception e) {
                log.warn("[IpAccessLog] flush 失败 key={}: {}", key, e.getMessage());
            }
        }
    }

    public List<Map<String, Object>> getDailyTrend(int days) {
        return ipAccessLogMapper.selectDailyTrend(LocalDate.now().minusDays(Math.max(days, 1) - 1L));
    }

    public List<Map<String, Object>> getDailyByIp(String ip, int days) {
        return ipAccessLogMapper.selectDailyByIp(ip, LocalDate.now().minusDays(Math.max(days, 1) - 1L));
    }

    /** Top 试探 IP，附带"当前是否已封禁" */
    public List<Map<String, Object>> getTopIps(int days, int size) {
        List<Map<String, Object>> list = ipAccessLogMapper.selectTopIps(
                LocalDate.now().minusDays(Math.max(days, 1) - 1L), Math.min(Math.max(size, 1), 200));
        for (Map<String, Object> row : list) {
            Object ip = row.get("ip");
            row.put("banned", ip != null && ipBlacklistService.isBlacklisted(ip.toString()));
        }
        return list;
    }

    public PageResult<IpAccessLogDO> getPage(IpAccessLogPageReqVO reqVO) {
        return ipAccessLogMapper.selectPage(reqVO, new LambdaQueryWrapperX<IpAccessLogDO>()
                .likeIfPresent(IpAccessLogDO::getIp, reqVO.getIp())
                .eqIfPresent(IpAccessLogDO::getReason, reqVO.getReason())
                .geIfPresent(IpAccessLogDO::getStatDay, reqVO.getDayStart())
                .leIfPresent(IpAccessLogDO::getStatDay, reqVO.getDayEnd())
                .orderByDesc(IpAccessLogDO::getUpdateTime));
    }
}
