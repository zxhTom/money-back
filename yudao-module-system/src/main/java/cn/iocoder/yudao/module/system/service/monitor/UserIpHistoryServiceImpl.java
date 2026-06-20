package cn.iocoder.yudao.module.system.service.monitor;

import cn.iocoder.yudao.module.system.dal.dataobject.monitor.UserIpHistoryDO;
import cn.iocoder.yudao.module.system.dal.mysql.monitor.UserIpHistoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class UserIpHistoryServiceImpl implements UserIpHistoryService {

    @Resource
    private UserIpHistoryMapper userIpHistoryMapper;

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

    @Override
    public List<UserIpHistoryDO> getByUserId(Long userId) {
        return userIpHistoryMapper.selectByUserId(userId);
    }
}
