package cn.iocoder.yudao.module.custom.service.changelog;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.custom.controller.admin.changelog.vo.VersionChangelogCheckRespVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.changelog.VersionChangelogDO;
import cn.iocoder.yudao.module.custom.dal.mysql.changelog.VersionChangelogMapper;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class VersionChangelogServiceImpl implements VersionChangelogService {

    @Resource
    private VersionChangelogMapper versionChangelogMapper;
    @Resource
    private AdminUserMapper adminUserMapper;

    @Override
    public VersionChangelogCheckRespVO check(Long userId, String version) {
        VersionChangelogDO changelog = versionChangelogMapper.selectByVersion(version);
        if (changelog == null || !Integer.valueOf(1).equals(changelog.getEnabled())) {
            return VersionChangelogCheckRespVO.notShow();
        }
        AdminUserDO user = adminUserMapper.selectById(userId);
        String lastSeen = user != null ? user.getLastSeenChangelogVersion() : null;
        if (StrUtil.equals(lastSeen, version)) {
            return VersionChangelogCheckRespVO.notShow();
        }
        return VersionChangelogCheckRespVO.show(changelog.getTitle(), changelog.getContent());
    }

    @Override
    public void ack(Long userId, String version) {
        AdminUserDO update = new AdminUserDO();
        update.setId(userId);
        update.setLastSeenChangelogVersion(version);
        adminUserMapper.updateById(update);
    }

    @Override
    public List<VersionChangelogDO> listAll() {
        return versionChangelogMapper.selectAllOrderByCreateTimeDesc();
    }

    @Override
    public VersionChangelogDO getById(Long id) {
        return versionChangelogMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(VersionChangelogDO changelog) {
        changelog.setId(null);
        versionChangelogMapper.insert(changelog);
        return changelog.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(VersionChangelogDO changelog) {
        versionChangelogMapper.updateById(changelog);
    }

    @Override
    public void delete(Long id) {
        versionChangelogMapper.deleteById(id);
    }

    @Override
    public void setEnabled(Long id, boolean enabled) {
        VersionChangelogDO update = new VersionChangelogDO();
        update.setId(id);
        update.setEnabled(enabled ? 1 : 0);
        versionChangelogMapper.updateById(update);
    }

}
