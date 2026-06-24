package cn.iocoder.yudao.module.system.service.monitor;

import cn.iocoder.yudao.module.system.dal.dataobject.monitor.UserIpHistoryDO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.UserIpStatDO;
import cn.iocoder.yudao.module.system.dal.mysql.logger.LoginLogMapper;
import cn.iocoder.yudao.module.system.dal.mysql.logger.OperateLogMapper;
import cn.iocoder.yudao.module.system.dal.mysql.monitor.DataAccessLogMapper;
import cn.iocoder.yudao.module.system.dal.mysql.monitor.UserIpHistoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class UserIpHistoryServiceImpl implements UserIpHistoryService {

    @Resource
    private UserIpHistoryMapper userIpHistoryMapper;
    @Resource
    private LoginLogMapper loginLogMapper;
    @Resource
    private OperateLogMapper operateLogMapper;
    @Resource
    private DataAccessLogMapper dataAccessLogMapper;

    @Override
    @Async
    public void record(Long userId, String ip) {
        if (userId == null || !StringUtils.hasText(ip)) return;
        try {
            userIpHistoryMapper.upsert(userId, ip);
        } catch (Exception e) {
            log.warn("[UserIpHistory] 记录用户IP失败 userId={} ip={}", userId, ip, e);
        }
    }

    /**
     * 与 IP 诊断同源：聚合登录日志、操作日志、数据访问日志三张表的 IP 使用情况，
     * 避免该接口只反映登录 IP、与诊断结果不一致。
     */
    @Override
    public List<UserIpHistoryDO> getByUserId(Long userId) {
        Map<String, UserIpHistoryDO> merged = new LinkedHashMap<>();
        mergeStats(loginLogMapper.selectIpStatsByUserId(userId), userId, merged);
        mergeStats(operateLogMapper.selectIpStatsByUserId(userId), userId, merged);
        mergeStats(dataAccessLogMapper.selectIpStatsByUserId(userId), userId, merged);

        List<UserIpHistoryDO> result = new ArrayList<>(merged.values());
        result.sort(Comparator.comparing(UserIpHistoryDO::getLastSeen).reversed());
        return result;
    }

    private void mergeStats(List<UserIpStatDO> stats, Long userId, Map<String, UserIpHistoryDO> merged) {
        if (stats == null) return;
        for (UserIpStatDO stat : stats) {
            UserIpHistoryDO existing = merged.get(stat.getIp());
            if (existing == null) {
                UserIpHistoryDO history = new UserIpHistoryDO();
                history.setUserId(userId);
                history.setIp(stat.getIp());
                history.setFirstSeen(stat.getFirstSeen());
                history.setLastSeen(stat.getLastSeen());
                history.setCount(stat.getCount());
                merged.put(stat.getIp(), history);
            } else {
                existing.setCount(existing.getCount() + stat.getCount());
                if (stat.getFirstSeen().isBefore(existing.getFirstSeen())) {
                    existing.setFirstSeen(stat.getFirstSeen());
                }
                if (stat.getLastSeen().isAfter(existing.getLastSeen())) {
                    existing.setLastSeen(stat.getLastSeen());
                }
            }
        }
    }
}
