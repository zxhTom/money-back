package cn.iocoder.yudao.module.custom.security;

import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.custom.controller.admin.recycle.ContractRecycleController;
import cn.iocoder.yudao.module.custom.framework.clickhouse.core.ClickHouseArchiveService;
import cn.iocoder.yudao.module.system.framework.idcard.*;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import org.junit.jupiter.api.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.RedisScript;
import java.lang.reflect.Field;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecycleIdentitySecurityTest {
    private ContractRecycleController controller;
    private SecurityFrameworkService security;
    private ClickHouseArchiveService archive;
    private IdCardCipherService cipher;
    private Map<String,Object> stored;
    private List<Object[]> queries;
    static void set(Object object, String field, Object value) throws Exception {
        Field f=object.getClass().getDeclaredField(field); f.setAccessible(true); f.set(object,value);
    }
    @BeforeEach void setup() throws Exception {
        TenantContextHolder.setTenantId(1L);
        security=mock(SecurityFrameworkService.class);
        when(security.hasAnyRoles("super_admin","tenant_admin","contract-manager")).thenReturn(true);
        when(security.hasPermission("custom:contract:recycle")).thenReturn(true);
        stored=new LinkedHashMap<>();stored.put("id",7L);stored.put("indebted_name","Synthetic Borrower");stored.put("creditor_name","Synthetic Lender");
        stored.put("indebted_id","110101199001010010");stored.put("creditor_id","110101199002020020");
        queries=new ArrayList<>();
        archive=mock(ClickHouseArchiveService.class, invocation->{
            if(invocation.getMethod().getName().equals("isEnabled"))return true;
            if(invocation.getMethod().getName().equals("query")) {
                queries.add(invocation.getArguments());
                return ((String)invocation.getArgument(0)).startsWith("SELECT count()")
                    ? Collections.singletonList(Collections.singletonMap("c",1L)) : Collections.singletonList(stored);
            }
            return org.mockito.Answers.RETURNS_DEFAULTS.answer(invocation);
        });
        cipher=new IdCardCipherService();IdCardCipherProperties props=new IdCardCipherProperties();props.getCipher().setSecret("synthetic-test-key-only-32-characters");props.getDisplay().setPlainForIdNoDisplay(true);
        set(cipher,"properties",props);
        controller=new ContractRecycleController();set(controller,"ch",archive);set(controller,"securityFrameworkService",security);set(controller,"idCardCipherService",cipher);
    }
    @AfterEach void cleanup(){TenantContextHolder.clear();}
    @Test void responseEncryptsBothIdsEvenWhenLegacyPlainDisplayIsEnabled() {
        Map<String,Object> response=controller.page(1,10,null).getData();
        Map<String,Object> row=(Map<String,Object>)((List<?>)response.get("list")).get(0);
        for(String field:Arrays.asList("indebted_id","creditor_id")){
            assertNotEquals(stored.get(field),row.get(field));assertEquals(stored.get(field),cipher.decryptOrNull((String)row.get(field)));
        }
        assertEquals("Synthetic Borrower",row.get("indebted_name"));assertEquals(7L,row.get("id"));assertEquals(1L,response.get("total"));
    }
    @Test void responseTransformationNeverMutatesArchivedValuesUsedForRestore() {
        controller.page(1,10,null);assertEquals("110101199001010010",stored.get("indebted_id"));assertEquals("110101199002020020",stored.get("creditor_id"));
    }
    @Test void historicalEncryptedArchiveValuesRemainReadableWithoutDoubleEncryption() {
        String old=cipher.requestToStoredCipher((String)stored.get("creditor_id"));stored.put("creditor_id",old);
        Map<String,Object> response=controller.page(1,10,null).getData();Map<?,?> row=(Map<?,?>)((List<?>)response.get("list")).get(0);
        assertEquals("110101199002020020",cipher.decryptOrNull((String)row.get("creditor_id")));assertEquals(old,stored.get("creditor_id"));
    }
    @Test void ordinaryRoleCannotReadRestoreOrDeleteEvenWithLegacyMenuPermission() {
        when(security.hasAnyRoles("super_admin","tenant_admin","contract-manager")).thenReturn(false);
        assertThrows(AccessDeniedException.class,()->controller.page(1,10,null));
        assertThrows(AccessDeniedException.class,()->controller.restore(Collections.singletonList(7L)));
        assertThrows(AccessDeniedException.class,()->controller.delete(Collections.singletonList(7L)));verifyNoInteractions(archive);
    }
    @Test void adminRoleWithoutRecyclePermissionCannotRead() {
        when(security.hasPermission("custom:contract:recycle")).thenReturn(false);assertThrows(AccessDeniedException.class,()->controller.page(1,10,null));verifyNoInteractions(archive);
    }
    @Test void disabledArchiveStillChecksAuthorizationAndReturnsCompatibleEmptyShape() {
        when(archive.isEnabled()).thenReturn(false);Map<String,Object> response=controller.page(1,10,null).getData();assertEquals(Collections.emptyList(),response.get("list"));assertEquals(0,response.get("total"));assertTrue(queries.isEmpty());
    }
    @Test void existingEmptyIdentityFieldsDoNotExposePlaintext() {
        stored.put("indebted_id",null);stored.put("creditor_id","");Map<String,Object> response=controller.page(1,10,null).getData();Map<?,?> row=(Map<?,?>)((List<?>)response.get("list")).get(0);assertNull(row.get("indebted_id"));assertNull(row.get("creditor_id"));
    }
    @Test void serverClampsPageSizeAndAvoidsOffsetOverflow() {
        controller.page(Integer.MAX_VALUE,Integer.MAX_VALUE,null);Object[] args=queries.get(1);
        Object[] params=args.length==2 && args[1] instanceof Object[] ? (Object[])args[1] : Arrays.copyOfRange(args,1,args.length);
        assertEquals(1L,params[0]);assertEquals(100,params[1]);assertEquals(214748364600L,params[2]);
    }
    @Test void currentZeroRequestLimitRejectsTheVeryFirstRequest() throws Exception {
        IdCardDecryptAccessLimiter limiter=new IdCardDecryptAccessLimiter();IdCardCipherProperties props=new IdCardCipherProperties();props.getDecryptApi().setRateMaxRequests(0);props.getDecryptApi().setRateWindowSeconds(9000000);
        StringRedisTemplate redis=mock(StringRedisTemplate.class);ValueOperations<String,String> ops=mock(ValueOperations.class);when(redis.opsForValue()).thenReturn(ops);when(ops.increment("idcard:dec:rate:42")).thenReturn(1L);
        set(limiter,"stringRedisTemplate",redis);set(limiter,"idCardCipherProperties",props);
        assertThrows(ServiceException.class,()->limiter.checkRequestRate(42L));
    }
}
