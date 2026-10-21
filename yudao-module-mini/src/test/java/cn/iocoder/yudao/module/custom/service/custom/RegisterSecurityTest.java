package cn.iocoder.yudao.module.custom.service.custom;

import cn.iocoder.yudao.module.custom.controller.admin.custom.vo.RegisterReqVO;
import cn.iocoder.yudao.module.custom.service.invite.InviteCodeService;
import cn.iocoder.yudao.module.custom.service.invite.RegisterRiskControlGuard;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper;
import cn.iocoder.yudao.module.system.framework.idcard.IdCardCipherService;
import cn.iocoder.yudao.module.system.service.dept.DeptService;
import cn.iocoder.yudao.module.system.service.permission.RoleService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterSecurityTest {
    @Mock private InviteCodeService inviteCodeService;
    @Mock private RegisterRiskControlGuard registerRiskControlGuard;
    @Mock private AdminUserMapper adminUserMapper;
    @Mock private AdminUserService userService;
    @Mock private IdCardCipherService idCardCipherService;
    @Mock private DeptService deptService;
    @Mock private RoleService roleService;
    @InjectMocks private CustomDefineServiceImpl service;

    @Test
    void publicRequestDoesNotBindInternalFields() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        RegisterReqVO request = mapper.readValue("{\"realname\":\"测试用户\",\"password\":\"test-password\","
                + "\"idCard\":\"identity\",\"inviteCode\":\"INVITE\",\"verified\":1,\"tenantId\":999,"
                + "\"deptId\":1,\"postIds\":[1],\"inviteEnabled\":true,\"payPasswordChanged\":true}", RegisterReqVO.class);
        String serialized = mapper.writeValueAsString(request);
        assertEquals("identity", request.getIdCard());
        assertEquals("INVITE", request.getInviteCode());
        for (String forbidden : new String[]{"verified", "tenantId", "deptId", "postIds", "inviteEnabled", "payPasswordChanged"}) {
            assertFalse(serialized.contains("\"" + forbidden + "\""), forbidden);
        }
    }

    @Test
    void serviceDiscardsInjectedPrivilegesAndStartsUnverified() {
        AdminUserDO request = profile();
        request.setVerified(1);
        request.setTenantId(999L);
        request.setDeptId(123L);
        request.setInviteEnabled(true);
        request.setDisablePwdChange(true);
        request.setTeaseEnabled(true);
        request.setPayPasswordChanged(true);
        when(idCardCipherService.normalizeRequestToCipher(anyString())).thenAnswer(i -> i.getArgument(0));

        assertEquals(1, service.register(request));

        ArgumentCaptor<AdminUserDO> saved = ArgumentCaptor.forClass(AdminUserDO.class);
        verify(userService).insertUserSimply(saved.capture());
        AdminUserDO user = saved.getValue();
        assertEquals(0, user.getVerified());
        assertNull(user.getTenantId());
        assertNull(user.getDeptId());
        assertFalse(user.getInviteEnabled());
        assertFalse(user.getDisablePwdChange());
        assertFalse(user.getTeaseEnabled());
        assertFalse(user.getPayPasswordChanged());
        assertEquals("测试用户", user.getRealname());
        assertEquals("identity", user.getIdNo());
        assertEquals("test-password", user.getPassword());
    }

    @Test
    void legacyIdCardInputStillRegistersWithServerAssignedDepartment() {
        AdminUserDO request = profile();
        request.setIdNo(null);
        request.setIdCard("identity");
        when(idCardCipherService.normalizeRequestToCipher(anyString())).thenAnswer(i -> i.getArgument(0));
        cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO dept = new cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO();
        dept.setId(88L);
        when(deptService.getDeptByName("合同管理部")).thenReturn(dept);
        service.register(request);
        ArgumentCaptor<AdminUserDO> saved = ArgumentCaptor.forClass(AdminUserDO.class);
        verify(userService).insertUserSimply(saved.capture());
        assertEquals("identity", saved.getValue().getIdNo());
        assertEquals(88L, saved.getValue().getDeptId());
        assertEquals(0, saved.getValue().getVerified());
    }

    private AdminUserDO profile() {
        AdminUserDO request = new AdminUserDO();
        request.setRealname("测试用户");
        request.setNickname("测试昵称");
        request.setPassword("test-password");
        request.setIdNo("identity");
        return request;
    }
}
