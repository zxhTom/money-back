package cn.iocoder.yudao.module.custom.controller.admin.invite;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.custom.controller.admin.invite.vo.InviteCodeRespVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.invite.InviteCodeDO;
import cn.iocoder.yudao.module.custom.service.invite.InviteCodeService;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InviteAdminControllerTest {

    @Mock
    private InviteCodeService inviteCodeService;

    @Mock
    private AdminUserService adminUserService;

    @InjectMocks
    private InviteAdminController controller;

    @BeforeEach
    public void setUp() {
        TenantContextHolder.clear();
    }

    @AfterEach
    public void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    public void test1_GetUserExists_ReturnsListSuccessfully() {
        Long userId = 100L;
        AdminUserDO user = new AdminUserDO();
        user.setId(userId);
        when(adminUserService.getUser(userId)).thenReturn(user);

        InviteCodeDO codeDO = new InviteCodeDO();
        codeDO.setId(1L);
        codeDO.setCode("ABC12345");
        codeDO.setInviterUserId(userId);
        when(inviteCodeService.listCodesByInviter(userId)).thenReturn(Collections.singletonList(codeDO));

        CommonResult<List<InviteCodeRespVO>> response = controller.getCodes(userId);
        assertNotNull(response);
        assertEquals(0, response.getCode());
        assertEquals(1, response.getData().size());
        assertEquals("ABC12345", response.getData().get(0).getCode());
    }

    @Test
    public void test2_GetUserNull_ThrowsExceptionAndServiceNotCalled() {
        Long userId = 200L;
        when(adminUserService.getUser(userId)).thenReturn(null);

        assertThrows(ServiceException.class, () -> controller.getCodes(userId));
        verify(inviteCodeService, never()).listCodesByInviter(any());
    }

    @Test
    public void test3_GetRegistrations_InviterUserIdNull_ThrowsException() {
        Long codeId = 10L;
        cn.iocoder.yudao.module.custom.dal.dataobject.invite.InviteRegisterLogDO regDO =
                new cn.iocoder.yudao.module.custom.dal.dataobject.invite.InviteRegisterLogDO();
        regDO.setId(100L);
        regDO.setInviterUserId(null);
        when(inviteCodeService.listRegistrations(codeId)).thenReturn(Collections.singletonList(regDO));

        assertThrows(ServiceException.class, () -> controller.getRegistrations(codeId));
    }
}
