package cn.iocoder.yudao.module.custom.service.changelog;

import cn.iocoder.yudao.module.custom.controller.admin.changelog.vo.VersionChangelogCheckRespVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.changelog.VersionChangelogDO;

import java.util.List;

public interface VersionChangelogService {

    /** 查是否需要给该用户弹出 version 对应的公告；只读，不写库 */
    VersionChangelogCheckRespVO check(Long userId, String version);

    /** 用户确认已看到弹窗后调用，写入 lastSeenChangelogVersion */
    void ack(Long userId, String version);

    List<VersionChangelogDO> listAll();

    VersionChangelogDO getById(Long id);

    Long create(VersionChangelogDO changelog);

    void update(VersionChangelogDO changelog);

    void delete(Long id);

    void setEnabled(Long id, boolean enabled);

}
