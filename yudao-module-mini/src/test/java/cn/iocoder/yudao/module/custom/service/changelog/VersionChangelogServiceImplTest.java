package cn.iocoder.yudao.module.custom.service.changelog;

import cn.iocoder.yudao.module.custom.controller.admin.changelog.vo.VersionChangelogCheckRespVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.changelog.VersionChangelogDO;
import cn.iocoder.yudao.module.custom.dal.mysql.changelog.VersionChangelogMapper;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VersionChangelogServiceImplTest {

    @Mock
    private VersionChangelogMapper versionChangelogMapper;
    @Mock
    private AdminUserMapper adminUserMapper;

    @InjectMocks
    private VersionChangelogServiceImpl service;

    private VersionChangelogDO changelog(String version, boolean enabled) {
        VersionChangelogDO c = new VersionChangelogDO();
        c.setVersion(version);
        c.setTitle("新版本来啦");
        c.setContent("1. 修复了一些问题\n2. 优化了体验");
        c.setEnabled(enabled ? 1 : 0);
        return c;
    }

    private AdminUserDO user(String lastSeenVersion) {
        AdminUserDO u = new AdminUserDO();
        u.setId(9L);
        u.setLastSeenChangelogVersion(lastSeenVersion);
        return u;
    }

    @Test
    public void testCheck_noChangelogForVersion_shouldShowFalse() {
        when(versionChangelogMapper.selectByVersion("1.5.0")).thenReturn(null);

        VersionChangelogCheckRespVO resp = service.check(9L, "1.5.0");

        assertFalse(resp.getShouldShow());
        assertNull(resp.getTitle());
        assertNull(resp.getContent());
    }

    @Test
    public void testCheck_changelogDisabled_shouldShowFalse() {
        when(versionChangelogMapper.selectByVersion("1.5.0")).thenReturn(changelog("1.5.0", false));

        VersionChangelogCheckRespVO resp = service.check(9L, "1.5.0");

        assertFalse(resp.getShouldShow());
    }

    @Test
    public void testCheck_alreadySeenThisVersion_shouldShowFalse() {
        when(versionChangelogMapper.selectByVersion("1.5.0")).thenReturn(changelog("1.5.0", true));
        when(adminUserMapper.selectById(9L)).thenReturn(user("1.5.0"));

        VersionChangelogCheckRespVO resp = service.check(9L, "1.5.0");

        assertFalse(resp.getShouldShow());
    }

    @Test
    public void testCheck_neverSeenThisVersion_shouldShowTrueWithContent() {
        when(versionChangelogMapper.selectByVersion("1.5.0")).thenReturn(changelog("1.5.0", true));
        when(adminUserMapper.selectById(9L)).thenReturn(user("1.4.0")); // 看过上一个版本，没看过这个

        VersionChangelogCheckRespVO resp = service.check(9L, "1.5.0");

        assertTrue(resp.getShouldShow());
        assertEquals("新版本来啦", resp.getTitle());
        assertEquals("1. 修复了一些问题\n2. 优化了体验", resp.getContent());
    }

    @Test
    public void testCheck_userNeverSeenAnyVersion_lastSeenNull_shouldShowTrue() {
        when(versionChangelogMapper.selectByVersion("1.5.0")).thenReturn(changelog("1.5.0", true));
        when(adminUserMapper.selectById(9L)).thenReturn(user(null));

        VersionChangelogCheckRespVO resp = service.check(9L, "1.5.0");

        assertTrue(resp.getShouldShow());
    }

    @Test
    public void testCheck_userNotFound_doesNotThrow_shouldShowTrue() {
        // 极端情况兜底：查不到用户就当作没看过处理，不抛异常影响小程序正常使用
        when(versionChangelogMapper.selectByVersion("1.5.0")).thenReturn(changelog("1.5.0", true));
        when(adminUserMapper.selectById(9L)).thenReturn(null);

        VersionChangelogCheckRespVO resp = service.check(9L, "1.5.0");

        assertTrue(resp.getShouldShow());
    }

    @Test
    public void testAck_updatesLastSeenChangelogVersion() {
        service.ack(9L, "1.5.0");

        ArgumentCaptor<AdminUserDO> captor = ArgumentCaptor.forClass(AdminUserDO.class);
        verify(adminUserMapper).updateById(captor.capture());
        assertEquals(9L, captor.getValue().getId());
        assertEquals("1.5.0", captor.getValue().getLastSeenChangelogVersion());
    }
}
