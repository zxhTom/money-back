package cn.iocoder.yudao.module.custom.service.contract;

import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractSaveReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.custom.dal.mysql.contract.ContractMapper;
import org.junit.jupiter.api.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContractAttachmentReferenceGuardTest {
    private ContractAttachmentReferenceGuard guard;
    private SecurityFrameworkService security;
    private String file(long owner) {return "https://org.toms.chat/admin-api/infra/file/1/get/private/contracts/"+owner+"/20261021/test.png";}
    @BeforeEach void setup() throws Exception {
        guard=new ContractAttachmentReferenceGuard();security=mock(SecurityFrameworkService.class);inject(guard,"security",security);
        SecurityFrameworkUtils.setLoginUser(new LoginUser().setId(42L).setUserType(2),mock(HttpServletRequest.class));
    }
    @AfterEach void cleanup() {SecurityContextHolder.clearContext();}
    private static void inject(Object target,String name,Object value) throws Exception {Field f=target.getClass().getDeclaredField(name);f.setAccessible(true);f.set(target,value);}
    @Test void ownUploadCanBeAttached() {assertDoesNotThrow(() -> guard.validate(file(42),null));}
    @Test void foreignPrivateUploadCannotBeAttached() {assertThrows(AccessDeniedException.class,() -> guard.validate(file(99),null));}
    @Test void encodedForeignPathDoesNotBypassOwnership() {assertThrows(AccessDeniedException.class,() -> guard.validate(file(99).replace("private","%70rivate"),null));}
    @Test void unchangedCounterpartyAttachmentKeepsWorkingWhenSignatureRotates() {
        assertDoesNotThrow(() -> guard.validate(file(99)+"?signature=new&expires=2",file(99)+"?signature=old&expires=1"));
    }
    @Test void differentForeignAttachmentCannotReuseExistingReference() {
        assertThrows(AccessDeniedException.class,() -> guard.validate(file(99).replace("test.png","other.png"),file(99)));
    }
    @Test void managementKeepsAttachmentManagement() {
        when(security.hasAnyRoles("super_admin")).thenReturn(true);
        assertDoesNotThrow(() -> guard.validate(file(99),null));
    }
    @Test void emptyAndLegacyPublicAttachmentsKeepWorking() {
        assertDoesNotThrow(() -> guard.validate(null,null));assertDoesNotThrow(() -> guard.validate("",file(99)));
        assertThrows(AccessDeniedException.class,() -> guard.validate("https://org.toms.chat/legacy/test.png",null));
        assertDoesNotThrow(() -> guard.validate("https://org.toms.chat/legacy/test.png","https://org.toms.chat/legacy/test.png"));
    }
    @Test void traversalCannotReuseAnotherPrivateDirectory() {
        assertThrows(AccessDeniedException.class,() -> guard.validate(file(42).replace("/20261021/","/../99/"),null));
    }
    @Test void actualContractCreationRejectsForeignReferenceBeforeAnyWrite() throws Exception {
        ContractServiceImpl service=new ContractServiceImpl();ContractMapper mapper=mock(ContractMapper.class);
        inject(service,"attachmentReferenceGuard",guard);inject(service,"contractMapper",mapper);
        ContractSaveReqVO input=new ContractSaveReqVO();input.setFile(file(99));
        assertThrows(AccessDeniedException.class,() -> service.createContract(input));verifyNoInteractions(mapper);
    }
    @Test void actualContractUpdatePreservesOldReferenceButRejectsReplacement() throws Exception {
        ContractServiceImpl service=new ContractServiceImpl();ContractMapper mapper=mock(ContractMapper.class);
        inject(service,"attachmentReferenceGuard",guard);inject(service,"contractMapper",mapper);
        inject(service,"ownershipGuard",mock(ContractOwnershipGuard.class));
        inject(service,"idCardCipherService",mock(cn.iocoder.yudao.module.system.framework.idcard.IdCardCipherService.class));
        ContractDO existing=new ContractDO();existing.setId(10L);existing.setFile(file(99));when(mapper.selectById(10L)).thenReturn(existing);
        ContractSaveReqVO input=new ContractSaveReqVO();input.setId(10L);input.setFile(file(99)+"?signature=updated");
        assertDoesNotThrow(() -> service.updateContract(input));verify(mapper).updateById(any(ContractDO.class));
        clearInvocations(mapper);input.setFile(file(99).replace("test.png","other.png"));
        assertThrows(AccessDeniedException.class,() -> service.updateContract(input));verify(mapper,never()).updateById(any(ContractDO.class));
    }

    @Test void tenantAdministratorCanUseSameTenantUploadsButNotOtherTenants()throws Exception {
        when(security.hasAnyRoles("tenant_admin","contract-manager")).thenReturn(true);
        cn.iocoder.yudao.module.system.service.user.AdminUserService users=mock(cn.iocoder.yudao.module.system.service.user.AdminUserService.class);inject(guard,"users",users);
        cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder.setTenantId(1L);
        cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO owner=new cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO();owner.setTenantId(1L);when(users.getUser(99L)).thenReturn(owner);
        try {assertDoesNotThrow(()->guard.validate(file(99),null));owner.setTenantId(2L);assertThrows(AccessDeniedException.class,()->guard.validate(file(99),null));}
        finally {cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder.clear();}
    }
}
