package cn.iocoder.yudao.module.custom.service.invite;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.custom.dal.dataobject.invite.InviteCodeDO;
import cn.iocoder.yudao.module.custom.dal.mysql.invite.InviteCodeMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.invite.InviteRegisterLogMapper;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InviteCodeServiceImplTest {

    @Mock
    private InviteCodeMapper inviteCodeMapper;
    @Mock
    private InviteRegisterLogMapper inviteRegisterLogMapper;
    @Mock
    private AdminUserService adminUserService;

    @InjectMocks
    private InviteCodeServiceImpl service;

    private AdminUserDO user(Long id, Boolean inviteEnabled) {
        AdminUserDO u = new AdminUserDO();
        u.setId(id);
        u.setInviteEnabled(inviteEnabled);
        return u;
    }

    private InviteCodeDO activeCode(Long inviterId) {
        InviteCodeDO c = new InviteCodeDO();
        c.setId(1L);
        c.setInviterUserId(inviterId);
        c.setCode("ABCD2345");
        c.setStatus(InviteCodeDO.STATUS_ACTIVE);
        c.setExpireTime(LocalDateTime.now().plusHours(4));
        return c;
    }

    // ── generate ────────────────────────────────────────────────
    @Test
    public void testGenerate_notEnabled_throws() {
        when(adminUserService.getUser(9L)).thenReturn(user(9L, false));
        ServiceException ex = assertThrows(ServiceException.class, () -> service.generate(9L));
        assertEquals(10011, ex.getCode());
    }

    // 注：generate() 开启分支里构造 LambdaUpdateWrapper 需要 MyBatis 运行期 TableInfo，
    // 纯 Mockito 无法覆盖，留给集成测试；此处只覆盖"未开启即拒绝"这条安全分支。

    // ── validate ────────────────────────────────────────────────
    @Test
    public void testValidate_blank_throws() {
        assertEquals(10010, assertThrows(ServiceException.class, () -> service.validate("  ")).getCode());
    }

    @Test
    public void testValidate_notFound_throws() {
        when(inviteCodeMapper.selectByCode("XXXX")).thenReturn(null);
        assertEquals(10010, assertThrows(ServiceException.class, () -> service.validate("XXXX")).getCode());
    }

    @Test
    public void testValidate_expired_throws() {
        InviteCodeDO c = activeCode(9L);
        c.setExpireTime(LocalDateTime.now().minusMinutes(1));
        when(inviteCodeMapper.selectByCode("ABCD2345")).thenReturn(c);
        assertEquals(10010, assertThrows(ServiceException.class, () -> service.validate("ABCD2345")).getCode());
    }

    @Test
    public void testValidate_invalidStatus_throws() {
        InviteCodeDO c = activeCode(9L);
        c.setStatus(InviteCodeDO.STATUS_INVALID);
        when(inviteCodeMapper.selectByCode("ABCD2345")).thenReturn(c);
        assertEquals(10010, assertThrows(ServiceException.class, () -> service.validate("ABCD2345")).getCode());
    }

    @Test
    public void testValidate_inviterDisabled_throws() {
        when(inviteCodeMapper.selectByCode("ABCD2345")).thenReturn(activeCode(9L));
        when(adminUserService.getUser(9L)).thenReturn(user(9L, false));
        assertEquals(10010, assertThrows(ServiceException.class, () -> service.validate("ABCD2345")).getCode());
    }

    @Test
    public void testValidate_happy_returnsCode() {
        InviteCodeDO c = activeCode(9L);
        when(inviteCodeMapper.selectByCode("ABCD2345")).thenReturn(c);
        when(adminUserService.getUser(9L)).thenReturn(user(9L, true));
        assertSame(c, service.validate("ABCD2345"));
    }

}
