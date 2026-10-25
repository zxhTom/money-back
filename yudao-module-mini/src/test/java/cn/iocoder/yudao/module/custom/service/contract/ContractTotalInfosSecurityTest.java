package cn.iocoder.yudao.module.custom.service.contract;

import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.custom.controller.admin.custom.vo.ContractPageReqDtoVO;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractPageReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.custom.vo.TotalInfosRespVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.custom.dal.mysql.contract.ContractMapper;
import cn.iocoder.yudao.module.custom.service.custom.CreditQueryAccessChecker;
import cn.iocoder.yudao.module.custom.service.custom.CustomDefineServiceImpl;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.UserRoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.permission.RoleMapper;
import cn.iocoder.yudao.module.system.dal.mysql.permission.UserRoleMapper;
import cn.iocoder.yudao.module.system.framework.idcard.IdCardCipherService;
import cn.iocoder.yudao.module.system.service.permission.RoleService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.framework.tenant.config.TenantProperties;
import cn.iocoder.yudao.framework.tenant.core.db.TenantDatabaseInterceptor;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Import({ContractQueryScope.class})
public class ContractTotalInfosSecurityTest extends BaseDbUnitTest {

    @Resource
    private ContractMapper contractMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private ContractQueryScope contractQueryScope;

    @Resource
    private MybatisPlusInterceptor mybatisPlusInterceptor;

    @MockBean
    private CreditQueryAccessChecker creditQueryAccessChecker;

    @MockBean
    private RoleService roleService;

    private CustomDefineServiceImpl customDefineService;
    private IdCardCipherService idCardCipherService;
    private AdminUserService adminUserService;
    private ContractTeaseGuard teaseGuard;

    @PostConstruct
    public void initTenantInterceptor() {
        TenantProperties properties = new TenantProperties();
        TenantLineInnerInterceptor inner = new TenantLineInnerInterceptor(new TenantDatabaseInterceptor(properties));
        MyBatisUtils.addInterceptor(mybatisPlusInterceptor, inner, 0);
    }

    private static void injectField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("注入字段失败: " + fieldName, e);
        }
    }

    private void login(Long userId, Long tenantId) {
        if (userId != null) {
            LoginUser loginUser = new LoginUser();
            loginUser.setId(userId);
            loginUser.setUserType(2); // ADMIN
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(loginUser, null));
        } else {
            SecurityContextHolder.clearContext();
        }
        TenantContextHolder.setTenantId(tenantId);
    }

    @BeforeEach
    public void setUp() {
        idCardCipherService = mock(IdCardCipherService.class);
        adminUserService = mock(AdminUserService.class);
        teaseGuard = mock(ContractTeaseGuard.class);

        lenient().when(idCardCipherService.normalizeRequestToCipher(anyString())).thenAnswer(i -> i.getArgument(0));
        lenient().when(teaseGuard.isTeasing(any())).thenReturn(false);

        customDefineService = new CustomDefineServiceImpl();
        injectField(customDefineService, "contractMapper", contractMapper);
        injectField(customDefineService, "contractQueryScope", contractQueryScope);
        injectField(customDefineService, "idCardCipherService", idCardCipherService);
        injectField(customDefineService, "creditQueryAccessChecker", creditQueryAccessChecker);
        injectField(customDefineService, "adminUserService", adminUserService);
        injectField(customDefineService, "teaseGuard", teaseGuard);
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
        TenantContextHolder.clear();
    }

    @Test
    public void testTotalInfos_realH2DatabaseExecution_allSecurityScenarios() {
        // 租户 1, 用户 100 创建，有效合同
        ContractDO c1 = new ContractDO();
        c1.setTenantId(1L); c1.setCreator("100"); c1.setSalary(1000L); c1.setDeleted(false);
        c1.setIndebtedName("张三"); c1.setIndebtedId("ID_ZHANG");
        c1.setCreditorName("李四"); c1.setCreditorId("ID_LI");
        contractMapper.insert(c1);

        // 租户 1, 用户 100 创建，逻辑删除（deleted = true）
        ContractDO c2 = new ContractDO();
        c2.setTenantId(1L); c2.setCreator("100"); c2.setSalary(500L); c2.setDeleted(true);
        c2.setIndebtedName("张三"); c2.setIndebtedId("ID_ZHANG");
        c2.setCreditorName("李四"); c2.setCreditorId("ID_LI");
        contractMapper.insert(c2);

        // 租户 1, 用户 200（同租户他人）创建，有效合同 (欠款人王五)
        ContractDO c3 = new ContractDO();
        c3.setTenantId(1L); c3.setCreator("200"); c3.setSalary(2000L); c3.setDeleted(false);
        c3.setIndebtedName("王五"); c3.setIndebtedId("ID_WANG");
        c3.setCreditorName("李四"); c3.setCreditorId("ID_LI");
        contractMapper.insert(c3);

        // 租户 2（跨租户），有效合同
        ContractDO c4 = new ContractDO();
        c4.setTenantId(2L); c4.setCreator("100"); c4.setSalary(3000L); c4.setDeleted(false);
        c4.setIndebtedName("张三"); c4.setIndebtedId("ID_ZHANG");
        c4.setCreditorName("李四"); c4.setCreditorId("ID_LI");
        contractMapper.insert(c4);

        // 场景 1. 普通用户 100 登录租户 1 查询无筛选 totalInfos：仅应计算 c1 (1000.0)
        login(100L, 1L);
        when(roleService.getRoleList(anyList())).thenReturn(Collections.emptyList());
        when(roleService.hasAnySuperAdmin(anyList())).thenReturn(false);

        TotalInfosRespVO res1 = customDefineService.totalInfos(new ContractPageReqVO());
        assertEquals(1000.0, res1.getTotalSalary());

        // 场景 2. 超级管理员登录租户 1 查询无筛选 totalInfos：应计算 c1 (1000) + c3 (2000) = 3000.0 (排除已删除 c2 和跨租户 c4)
        RoleDO superRole = new RoleDO();
        superRole.setId(1L);
        superRole.setCode("super_admin");
        superRole.setStatus(0);
        UserRoleDO ur = new UserRoleDO();
        ur.setUserId(100L);
        ur.setRoleId(1L);
        userRoleMapper.insert(ur);

        when(roleService.getRoleList(anyList())).thenReturn(Collections.singletonList(superRole));
        when(roleService.hasAnySuperAdmin(anyList())).thenReturn(true);

        TotalInfosRespVO resAdmin = customDefineService.totalInfos(new ContractPageReqVO());
        assertEquals(3000.0, resAdmin.getTotalSalary());

        // 场景 3. 普通用户 100 带不匹配姓名筛选：应返回 0.0
        when(roleService.getRoleList(anyList())).thenReturn(Collections.emptyList());
        when(roleService.hasAnySuperAdmin(anyList())).thenReturn(false);
        ContractPageReqVO nameReq = new ContractPageReqVO();
        nameReq.setIndebtedName("赵六");
        TotalInfosRespVO resName = customDefineService.totalInfos(nameReq);
        assertEquals(0.0, resName.getTotalSalary());

        // 场景 4. 空集测试：不存在任何合同的用户 999 查询无筛选 totalInfos：返回 0.0
        login(999L, 1L);
        TotalInfosRespVO resEmpty = customDefineService.totalInfos(new ContractPageReqVO());
        assertEquals(0.0, resEmpty.getTotalSalary());

        // 场景 5. 身份授权校验与分页明细金额汇总一致性
        // 5.1 未实名认证用户调用 page，身份授权拒绝，返回空列表
        login(100L, 1L);
        AdminUserDO unverifiedUser = new AdminUserDO();
        unverifiedUser.setId(100L);
        unverifiedUser.setVerified(0); // 未认证
        when(adminUserService.getUser(100L)).thenReturn(unverifiedUser);

        ContractPageReqDtoVO pageReq = new ContractPageReqDtoVO();
        pageReq.setLoanType("borrow");
        List<ContractDO> unverifiedList = customDefineService.page(pageReq);
        assertTrue(unverifiedList.isEmpty());

        // 5.2 已实名认证用户 (姓名张三, 身份证ID_ZHANG) 查询本人借入分页合同：查出 c1
        AdminUserDO verifiedUser = new AdminUserDO();
        verifiedUser.setId(100L);
        verifiedUser.setVerified(1);
        verifiedUser.setRealname("张三");
        verifiedUser.setIdNo("ID_ZHANG");
        when(adminUserService.getUser(100L)).thenReturn(verifiedUser);

        List<ContractDO> pagedList = customDefineService.page(pageReq);
        assertEquals(1, pagedList.size());
        assertEquals(1000L, pagedList.get(0).getSalary());

        // 5.3 身份授权拒绝场景：普通用户查他人身份证 (ID_LI) 的合同汇总，触发 AccessDeniedException
        ContractPageReqVO unauthorizedReq = new ContractPageReqVO();
        unauthorizedReq.setIndebtedId("ID_LI");
        when(creditQueryAccessChecker.canQueryCredit("ID_LI")).thenReturn(false);
        try {
            customDefineService.totalInfos(unauthorizedReq);
            org.junit.jupiter.api.Assertions.fail("应该抛出 AccessDeniedException");
        } catch (org.springframework.security.access.AccessDeniedException expected) {
            assertEquals("无权限查询该欠款人合同数据", expected.getMessage());
        }

        // 5.4 身份授权通过场景：普通用户查本人身份证 (ID_ZHANG) 或拥有权限，totalInfos 计算汇总金额与分页结果一致 (1000.0)
        when(creditQueryAccessChecker.canQueryCredit("ID_ZHANG")).thenReturn(true);
        ContractPageReqVO filterReq = new ContractPageReqVO();
        filterReq.setIndebtedName("张三");
        filterReq.setIndebtedId("ID_ZHANG");
        TotalInfosRespVO filterTotal = customDefineService.totalInfos(filterReq);
        assertEquals(1000.0, filterTotal.getTotalSalary());
        double pageSum = pagedList.stream().mapToDouble(c -> c.getSalary() != null ? c.getSalary().doubleValue() : 0.0).sum();
        assertEquals(filterTotal.getTotalSalary(), pageSum);
    }
}
