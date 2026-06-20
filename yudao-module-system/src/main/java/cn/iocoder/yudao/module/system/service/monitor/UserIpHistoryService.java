package cn.iocoder.yudao.module.system.service.monitor;

import cn.iocoder.yudao.module.system.dal.dataobject.monitor.UserIpHistoryDO;

import java.util.List;

public interface UserIpHistoryService {

    void record(Long userId, String ip);

    List<UserIpHistoryDO> getByUserId(Long userId);
}
