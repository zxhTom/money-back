package cn.iocoder.yudao.module.custom.security;

import cn.iocoder.yudao.framework.common.pojo.*;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.system.framework.security.SensitiveRecordAccess;
import cn.iocoder.yudao.module.system.controller.admin.user.PasswordHistoryController;
import cn.iocoder.yudao.module.system.controller.admin.user.vo.PasswordHistoryPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.PasswordHistoryDO;
import cn.iocoder.yudao.module.system.dal.mysql.user.PasswordHistoryMapper;
import cn.iocoder.yudao.module.custom.controller.admin.security.SecurityPwdResetLogController;
import cn.iocoder.yudao.module.custom.dal.dataobject.security.SecurityPwdResetLogDO;
import cn.iocoder.yudao.module.custom.dal.mysql.security.SecurityPwdResetLogMapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import java.lang.reflect.Field;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SensitiveRecordAuthorizationTest {
    SensitiveRecordAccess access; SecurityFrameworkService security;
    static void inject(Object o,String n,Object v)throws Exception {Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);f.set(o,v);}
    @BeforeAll static void tables() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),""),PasswordHistoryDO.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),""),SecurityPwdResetLogDO.class);
    }
    @BeforeEach void setup()throws Exception {
        LoginUser user=new LoginUser();user.setId(42L);user.setUserType(2);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user,null));TenantContextHolder.setTenantId(1L);
        access=new SensitiveRecordAccess();security=mock(SecurityFrameworkService.class);inject(access,"security",security);
    }
    @AfterEach void cleanup(){SecurityContextHolder.clearContext();TenantContextHolder.clear();}
    void admin(){when(security.hasAnyRoles("super_admin","tenant_admin","contract-manager")).thenReturn(true);}
    @Test void ordinaryScopeDefaultsToSelf(){assertEquals(42L,access.userFilter(null));assertEquals(42L,access.userFilter(42L));}
    @Test void ordinaryCannotRequestSomeoneElse(){assertThrows(AccessDeniedException.class,()->access.userFilter(99L));}
    @Test void administratorCanSelectSomeoneElseOrAll(){admin();assertEquals(99L,access.userFilter(99L));assertNull(access.userFilter(null));}
    @Test void tenantAdministratorStaysInItsTenant(){admin();assertTrue(access.tenantUserIdsSql().contains("tenant_id = 1"));}
    @Test void platformAdministratorKeepsGlobalAudit(){admin();when(security.hasAnyRoles("super_admin")).thenReturn(true);assertNull(access.tenantUserIdsSql());}
    @Test void anonymousCallerCannotUseEvenAdministratorFlags(){admin();SecurityContextHolder.clearContext();assertThrows(AccessDeniedException.class,()->access.userFilter(null));}
    @Test void missingTenantFailsClosed(){TenantContextHolder.clear();assertThrows(RuntimeException.class,access::tenantUserIdsSql);}
    PasswordHistoryDO history(){PasswordHistoryDO row=new PasswordHistoryDO();row.setUserId(42L);row.setPasswordHash("synthetic-own-hash");return row;}
    @Test void ordinaryHistoryQueryForcesSelfAndDoesNotExposeHash()throws Exception {
        PasswordHistoryController c=new PasswordHistoryController();PasswordHistoryMapper mapper=mock(PasswordHistoryMapper.class);inject(c,"recordAccess",access);inject(c,"passwordHistoryMapper",mapper);
        PasswordHistoryDO source=history();
        when(mapper.selectPage(any(PageParam.class),any(Wrapper.class))).thenAnswer(invocation->{
            com.baomidou.mybatisplus.core.conditions.AbstractWrapper query=invocation.getArgument(1);
            assertTrue(query.getSqlSegment().contains("user_id IN (SELECT id FROM system_users WHERE tenant_id = 1)"));
            assertTrue(query.getParamNameValuePairs().containsValue(42L));
            return new PageResult<>(Collections.singletonList(source),1L);
        });
        PageResult<PasswordHistoryDO> result=c.page(new PasswordHistoryPageReqVO()).getData();
        assertNull(result.getList().get(0).getPasswordHash());assertEquals("synthetic-own-hash",source.getPasswordHash());assertEquals(1L,result.getTotal());
    }
    @Test void historyRejectsForeignUserBeforeQuery()throws Exception {
        PasswordHistoryController c=new PasswordHistoryController();PasswordHistoryMapper mapper=mock(PasswordHistoryMapper.class);inject(c,"recordAccess",access);inject(c,"passwordHistoryMapper",mapper);
        PasswordHistoryPageReqVO req=new PasswordHistoryPageReqVO();req.setUserId(99L);
        assertThrows(AccessDeniedException.class,()->c.page(req));verifyNoInteractions(mapper);
    }
    @Test void administratorHistoryKeepsExistingFields()throws Exception {
        admin();PasswordHistoryController c=new PasswordHistoryController();PasswordHistoryMapper mapper=mock(PasswordHistoryMapper.class);inject(c,"recordAccess",access);inject(c,"passwordHistoryMapper",mapper);
        PasswordHistoryDO row=history();row.setUserId(99L);when(mapper.selectPage(any(PageParam.class),any(Wrapper.class))).thenAnswer(invocation -> {
            com.baomidou.mybatisplus.core.conditions.AbstractWrapper query=invocation.getArgument(1);query.getSqlSegment();
            assertTrue(query.getParamNameValuePairs().containsValue(99L));assertFalse(query.getParamNameValuePairs().containsValue(42L));
            return new PageResult<>(Collections.singletonList(row),1L);
        });
        PasswordHistoryPageReqVO req=new PasswordHistoryPageReqVO();req.setUserId(99L);
        assertEquals("synthetic-own-hash",c.page(req).getData().getList().get(0).getPasswordHash());
    }
    @Test void administratorCanStillViewOtherUsersResetPassword()throws Exception {
        admin();SecurityPwdResetLogController c=new SecurityPwdResetLogController();SecurityPwdResetLogMapper mapper=mock(SecurityPwdResetLogMapper.class);inject(c,"recordAccess",access);inject(c,"securityPwdResetLogMapper",mapper);
        SecurityPwdResetLogDO row=new SecurityPwdResetLogDO();row.setUserId(99L);row.setNewPassword("synthetic-reset-password");
        when(mapper.selectPage(any(PageParam.class),any(Wrapper.class))).thenReturn(new PageResult<>(Collections.singletonList(row),1L));
        assertEquals("synthetic-reset-password",c.getPage(1,10,99L).getData().getList().get(0).getNewPassword());
    }
    @Test void ordinaryResetQueryIsSelfScoped()throws Exception {
        SecurityPwdResetLogController c=new SecurityPwdResetLogController();SecurityPwdResetLogMapper mapper=mock(SecurityPwdResetLogMapper.class);inject(c,"recordAccess",access);inject(c,"securityPwdResetLogMapper",mapper);
        when(mapper.selectPage(any(PageParam.class),any(Wrapper.class))).thenAnswer(invocation->{
            com.baomidou.mybatisplus.core.conditions.AbstractWrapper query=invocation.getArgument(1);query.getSqlSegment();
            assertTrue(query.getParamNameValuePairs().containsValue(42L));return new PageResult<>(Collections.emptyList(),0L);
        });
        assertEquals(0L,c.getPage(1,10,null).getData().getTotal());
    }
    @Test void eachManagementRoleCanReadAnotherUsersRecords() {
        for (String code : Arrays.asList("super_admin", "tenant_admin", "contract-manager")) {
            when(security.hasAnyRoles(any(String[].class))).thenAnswer(invocation -> Arrays.asList(invocation.getArguments()).contains(code));
            assertEquals(99L,access.userFilter(99L),code);assertNull(access.userFilter(null),code);
            if ("super_admin".equals(code)) assertNull(access.tenantUserIdsSql());
            else assertTrue(access.tenantUserIdsSql().contains("tenant_id = 1"));
        }
    }
    @Test void administratorUnfilteredHistoryIsNotForcedToSelf()throws Exception {
        admin();PasswordHistoryController c=new PasswordHistoryController();PasswordHistoryMapper mapper=mock(PasswordHistoryMapper.class);inject(c,"recordAccess",access);inject(c,"passwordHistoryMapper",mapper);
        when(mapper.selectPage(any(PageParam.class),any(Wrapper.class))).thenAnswer(invocation -> {
            com.baomidou.mybatisplus.core.conditions.AbstractWrapper query=invocation.getArgument(1);query.getSqlSegment();
            assertFalse(query.getParamNameValuePairs().containsValue(42L));assertTrue(query.getSqlSegment().contains("tenant_id = 1"));
            return new PageResult<>(Collections.emptyList(),0L);
        });
        assertEquals(0L,c.page(new PasswordHistoryPageReqVO()).getData().getTotal());
    }
    @Test void ordinaryResetCannotFilterAnotherUser()throws Exception {
        SecurityPwdResetLogController c=new SecurityPwdResetLogController();SecurityPwdResetLogMapper mapper=mock(SecurityPwdResetLogMapper.class);inject(c,"recordAccess",access);inject(c,"securityPwdResetLogMapper",mapper);
        assertThrows(AccessDeniedException.class,()->c.getPage(1,10,99L));verifyNoInteractions(mapper);
    }
}
