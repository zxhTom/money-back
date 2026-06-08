package cn.iocoder.yudao.module.custom.service.security;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.custom.controller.admin.security.vo.IpBlacklistAddReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.security.IpBlacklistDO;

public interface IpBlacklistService {

    boolean isBlacklisted(String ip);

    void addToBlacklist(String ip, String reason, boolean autoAdded, java.time.LocalDateTime expireTime);

    void addToBlacklist(IpBlacklistAddReqVO reqVO);

    void removeFromBlacklist(Long id);

    PageResult<IpBlacklistDO> getPage(PageParam pageParam);

    void refreshCache();
}
