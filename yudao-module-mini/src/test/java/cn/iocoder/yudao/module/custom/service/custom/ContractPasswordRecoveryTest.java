package cn.iocoder.yudao.module.custom.service.custom;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.custom.controller.admin.custom.vo.ResetPasswordByIdNoReqVO;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.*;
import cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper;
import cn.iocoder.yudao.module.system.dal.mysql.permission.UserRoleMapper;
import cn.iocoder.yudao.module.system.framework.idcard.IdCardCipherService;
import cn.iocoder.yudao.module.system.service.permission.RoleService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import org.junit.jupiter.api.*;
import org.springframework.transaction.support.*;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class ContractPasswordRecoveryTest {
 ContractPasswordRecoveryService service;AdminUserMapper users;UserRoleMapper userRoles;RoleService roles;IdCardCipherService cipher;AdminUserService changes;ConfigApi config;RecoveryIpQuota quota;AdminUserDO user;ResetPasswordByIdNoReqVO input;
 static void inject(Object o,String name,Object value)throws Exception{java.lang.reflect.Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);f.set(o,value);}
 RoleDO role(long id,String code){RoleDO r=new RoleDO();r.setId(id);r.setCode(code);r.setStatus(0);return r;}
 @BeforeEach void setup()throws Exception {
  service=new ContractPasswordRecoveryService();users=mock(AdminUserMapper.class);userRoles=mock(UserRoleMapper.class);roles=mock(RoleService.class);cipher=mock(IdCardCipherService.class);changes=mock(AdminUserService.class);config=mock(ConfigApi.class);quota=mock(RecoveryIpQuota.class);
  inject(service,"adminUserMapper",users);inject(service,"userRoleMapper",userRoles);inject(service,"roleService",roles);inject(service,"idCardCipherService",cipher);inject(service,"adminUserService",changes);inject(service,"configApi",config);inject(service,"quota",quota);
  TenantContextHolder.setTenantId(1L);user=new AdminUserDO();user.setId(42L);user.setTenantId(1L);user.setStatus(0);user.setUsername("example");user.setIdNo("stored-id");
  input=new ResetPasswordByIdNoReqVO();input.setUsername(" example ");input.setIdNo("input-id");input.setPassword("Example789!");
  when(users.selectListByUsername("example")).thenReturn(Collections.singletonList(user));UserRoleDO relation=new UserRoleDO();relation.setUserId(42L);relation.setRoleId(159L);when(userRoles.selectListByUserId(42L)).thenReturn(Collections.singletonList(relation));when(roles.getRoleList(anyCollection())).thenReturn(Collections.singletonList(role(159,"contract")));when(cipher.sameIdCard("input-id","stored-id")).thenReturn(true);when(quota.reserve("192.0.2.1",3,5)).thenReturn("reservation");
 }
 @AfterEach void cleanup(){TenantContextHolder.clear();if(TransactionSynchronizationManager.isSynchronizationActive())TransactionSynchronizationManager.clearSynchronization();}
 @Test void exactUsernameAndIdentityContractUserCanResetBothPasswords(){service.reset(input,"192.0.2.1");verify(changes).updateUserPasswordAndPayPassword(42L,"Example789!");verify(quota).reserve("192.0.2.1",3,5);verify(quota,never()).release(anyString());}
 @Test void everyAdditionalRoleIncludingAdminDisqualifiesRecovery(){for(String code:Arrays.asList("super_admin","tenant_admin","contract-manager","creditor","common")){when(roles.getRoleList(anyCollection())).thenReturn(Arrays.asList(role(159,"contract"),role(1,code)));assertThrows(ServiceException.class,()->service.reset(input,"192.0.2.1"));}verifyNoInteractions(quota,changes);}
 @Test void identityMismatchAndUnknownUsernameNeverConsumeQuota(){when(cipher.sameIdCard(anyString(),anyString())).thenReturn(false);assertThrows(ServiceException.class,()->service.reset(input,"192.0.2.1"));when(users.selectListByUsername("example")).thenReturn(Collections.emptyList());assertThrows(ServiceException.class,()->service.reset(input,"192.0.2.1"));verifyNoInteractions(quota,changes);}
 @Test void disabledMissingRoleAndForeignTenantCannotRecover(){user.setStatus(1);assertThrows(ServiceException.class,()->service.reset(input,"192.0.2.1"));user.setStatus(0);user.setTenantId(162L);assertThrows(ServiceException.class,()->service.reset(input,"192.0.2.1"));user.setTenantId(1L);when(roles.getRoleList(anyCollection())).thenReturn(Collections.emptyList());assertThrows(ServiceException.class,()->service.reset(input,"192.0.2.1"));verifyNoInteractions(quota,changes);}
 @Test void quotaExceededCannotChangePassword(){when(quota.reserve(anyString(),anyInt(),anyInt())).thenThrow(new ServiceException(429,"limit"));assertThrows(ServiceException.class,()->service.reset(input,"192.0.2.1"));verifyNoInteractions(changes);}
 @Test void configurationIsReadAtRuntimeAndInvalidValueFailsClosed(){when(config.getConfigValueByKey(ContractPasswordRecoveryService.HOURS_KEY)).thenReturn("4");when(config.getConfigValueByKey(ContractPasswordRecoveryService.LIMIT_KEY)).thenReturn("7");service.reset(input,"192.0.2.1");verify(quota).reserve("192.0.2.1",4,7);reset(changes,quota);when(config.getConfigValueByKey(ContractPasswordRecoveryService.HOURS_KEY)).thenReturn("0");assertThrows(ServiceException.class,()->service.reset(input,"192.0.2.1"));verifyNoInteractions(changes,quota);}
 @Test void failedWriteReleasesReservation(){doThrow(new IllegalStateException("write failed")).when(changes).updateUserPasswordAndPayPassword(anyLong(),anyString());assertThrows(IllegalStateException.class,()->service.reset(input,"192.0.2.1"));verify(quota).release("reservation");}
 @Test void transactionRollbackReleasesButCommitKeepsQuota(){TransactionSynchronizationManager.initSynchronization();service.reset(input,"192.0.2.1");List<TransactionSynchronization> hooks=TransactionSynchronizationManager.getSynchronizations();assertEquals(1,hooks.size());hooks.get(0).afterCompletion(TransactionSynchronization.STATUS_COMMITTED);verify(quota,never()).release(anyString());hooks.get(0).afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);verify(quota).release("reservation");}
}
