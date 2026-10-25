package cn.iocoder.yudao.module.custom.service.custom;

import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.module.custom.dal.mysql.contract.ContractMapper;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.framework.idcard.IdCardCipherService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreditQueryAccessCheckerTest {

    @Mock
    private SecurityFrameworkService securityFrameworkService;

    @Mock
    private AdminUserService adminUserService;

    @Mock
    private IdCardCipherService idCardCipherService;

    @Mock
    private ContractMapper contractMapper;

    @InjectMocks
    private CreditQueryAccessChecker checker;

    private void loginAs(Long userId) {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(userId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null));
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testCanQueryCredit_hasPermission_bypassesRelationCheck() {
        when(securityFrameworkService.hasPermission("custom:contract:credit-query")).thenReturn(true);
        assertTrue(checker.canQueryCredit("someTargetIdNo"));
    }

    @Test
    public void testCanQueryCredit_noPermission_notLoggedIn_returnsFalse() {
        when(securityFrameworkService.hasPermission("custom:contract:credit-query")).thenReturn(false);
        SecurityContextHolder.clearContext();
        assertFalse(checker.canQueryCredit("someTargetIdNo"));
    }

    @Test
    public void testCanQueryCredit_noPermission_callerIdNoBlank_returnsFalse() {
        when(securityFrameworkService.hasPermission("custom:contract:credit-query")).thenReturn(false);
        loginAs(1L);
        AdminUserDO caller = new AdminUserDO();
        caller.setVerified(1);
        caller.setIdNo("");
        when(adminUserService.getUser(1L)).thenReturn(caller);
        assertFalse(checker.canQueryCredit("someTargetIdNo"));
    }

    @Test
    public void testCanQueryCredit_noPermission_hasContractRelation_stillReturnsFalse() {
        when(securityFrameworkService.hasPermission("custom:contract:credit-query")).thenReturn(false);
        loginAs(1L);
        AdminUserDO caller = new AdminUserDO();
        caller.setVerified(1);
        caller.setIdNo("callerCipher");
        when(adminUserService.getUser(1L)).thenReturn(caller);
        // 即使 caller 与 target 存在真实的合同往来关系（或者曾有 relation=true）
        when(idCardCipherService.sameIdCard("callerCipher", "targetCipher")).thenReturn(false);
        lenient().when(contractMapper.existsContractRelation(anyString(), anyString())).thenReturn(true);
        // 按新设计：完全移除 existsContractRelation 作为授权条件，必须拒绝！
        assertFalse(checker.canQueryCredit("targetCipher"));
        verifyNoInteractions(contractMapper);
    }

    @Test
    public void testCanQueryCredit_noPermission_noContractRelation_returnsFalse() {
        when(securityFrameworkService.hasPermission("custom:contract:credit-query")).thenReturn(false);
        loginAs(1L);
        AdminUserDO caller = new AdminUserDO();
        caller.setVerified(1);
        caller.setIdNo("callerCipher");
        when(adminUserService.getUser(1L)).thenReturn(caller);
        when(idCardCipherService.sameIdCard("callerCipher", "targetCipher")).thenReturn(false);
        assertFalse(checker.canQueryCredit("targetCipher"));
    }


    @Test
    public void unverifiedIdentityCannotUseRelationshipAsAuthorization() {
        loginAs(1L);AdminUserDO caller=new AdminUserDO();caller.setIdNo("callerCipher");caller.setVerified(0);
        when(adminUserService.getUser(1L)).thenReturn(caller);
        assertFalse(checker.canQueryCredit("targetCipher"));
        org.mockito.Mockito.verifyNoInteractions(contractMapper);
    }
    @Test
    public void verifiedUserCanQueryOwnCreditWithoutSelfContract() {
        loginAs(1L);AdminUserDO caller=new AdminUserDO();caller.setIdNo("callerCipher");caller.setVerified(1);
        when(adminUserService.getUser(1L)).thenReturn(caller);when(idCardCipherService.sameIdCard("callerCipher","sameIdentity")).thenReturn(true);
        assertTrue(checker.canQueryCredit("sameIdentity"));org.mockito.Mockito.verifyNoInteractions(contractMapper);
    }
}
