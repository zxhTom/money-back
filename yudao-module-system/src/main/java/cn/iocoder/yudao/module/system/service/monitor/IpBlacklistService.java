package cn.iocoder.yudao.module.system.service.monitor;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.IpBlacklistAddReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.IpBlacklistDO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.IpBlacklistLogDO;

import java.time.LocalDateTime;
import java.util.List;

public interface IpBlacklistService {

    boolean isBlacklisted(String ip);

    void addToBlacklist(String ip, String reason, boolean autoAdded, LocalDateTime expireTime);

    void addToBlacklist(IpBlacklistAddReqVO reqVO);

    void removeFromBlacklist(Long id);

    PageResult<IpBlacklistDO> getPage(PageParam pageParam);

    List<IpBlacklistLogDO> getBanLogs(String ip);

    void refreshCache();

    /** 查询某 IP 当前生效（status=0，未解封）的黑名单记录，用于展示具体封禁原因；找不到返回 null */
    IpBlacklistDO getActiveEntry(String ip);
}
