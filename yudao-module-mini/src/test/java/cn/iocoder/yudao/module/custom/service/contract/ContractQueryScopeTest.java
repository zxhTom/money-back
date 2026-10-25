package cn.iocoder.yudao.module.custom.service.contract;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractPageReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.custom.service.custom.CreditQueryAccessChecker;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.UserRoleDO;
import cn.iocoder.yudao.module.system.dal.mysql.permission.UserRoleMapper;
import cn.iocoder.yudao.module.system.service.permission.RoleService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContractQueryScopeTest {

    @Mock
    private CreditQueryAccessChecker creditQueryAccessChecker;

    @Mock
    private UserRoleMapper userRoleMapper;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private ContractQueryScope queryScope;

    private void login(Long userId) {
        if (userId != null) {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(new LoginUser().setId(userId), null));
        } else {
            SecurityContextHolder.clearContext();
        }
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testScope_ordinaryUserWithoutPartyFilter_restrictsToOwnCreator() {
        login(100L);
        when(userRoleMapper.selectListByUserId(100L)).thenReturn(Collections.emptyList());
        when(roleService.getRoleList(anyList())).thenReturn(Collections.emptyList());
        when(roleService.hasAnySuperAdmin(anyList())).thenReturn(false);

        ContractPageReqVO req = new ContractPageReqVO();
        ContractQueryScope.ScopeResult res = queryScope.resolveScope(req);
        assertEquals("100", res.getCreator());
        assertFalse(res.isSuperOrManager());
    }

    @Test
    public void testScope_superAdminWithoutPartyFilter_seesTenantWide() {
        login(100L);
        UserRoleDO ur = new UserRoleDO();
        ur.setRoleId(1L);
        when(userRoleMapper.selectListByUserId(100L)).thenReturn(Collections.singletonList(ur));
        RoleDO superRole = new RoleDO();
        superRole.setCode("super_admin");
        superRole.setStatus(0);
        when(roleService.getRoleList(anyList())).thenReturn(Collections.singletonList(superRole));
        when(roleService.hasAnySuperAdmin(anyList())).thenReturn(true);

        ContractPageReqVO req = new ContractPageReqVO();
        ContractQueryScope.ScopeResult res = queryScope.resolveScope(req);
        assertNull(res.getCreator());
        assertTrue(res.isSuperOrManager());
    }

    @Test
    public void testScope_withPartyFilter_deniesWhenNoCreditPermission() {
        login(100L);
        ContractPageReqVO req = new ContractPageReqVO();
        req.setIndebtedId("other-id");
        when(creditQueryAccessChecker.canQueryCredit("other-id")).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> queryScope.resolveScope(req));
    }

    @Test
    public void testScope_withPartyFilter_allowsWhenAuthorized() {
        login(100L);
        ContractPageReqVO req = new ContractPageReqVO();
        req.setIndebtedId("self-id");
        when(creditQueryAccessChecker.canQueryCredit("self-id")).thenReturn(true);

        when(userRoleMapper.selectListByUserId(100L)).thenReturn(Collections.emptyList());
        when(roleService.getRoleList(anyList())).thenReturn(Collections.emptyList());
        when(roleService.hasAnySuperAdmin(anyList())).thenReturn(false);

        ContractQueryScope.ScopeResult res = queryScope.resolveScope(req);
        assertNull(res.getCreator());
    }
}
