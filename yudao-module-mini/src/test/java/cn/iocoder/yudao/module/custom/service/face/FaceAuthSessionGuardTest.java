package cn.iocoder.yudao.module.custom.service.face;

import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.framework.idcard.IdCardCipherService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FaceAuthSessionGuardTest {

    @Mock
    private StringRedisTemplate redis;
    @Mock
    private ValueOperations<String, String> ops;
    @Mock
    private IdCardCipherService cipher;

    @InjectMocks
    private FaceAuthSessionGuard guard;

    private static void inject(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Test
    public void testGetTrustedOwner_validOwner() throws Exception {
        inject(guard, "redis", redis);
        when(redis.opsForValue()).thenReturn(ops);

        String hex64 = "a".repeat(64);
        when(ops.get(anyString())).thenReturn("1:100:" + hex64);

        FaceAuthSessionGuard.SessionOwner owner = guard.getTrustedOwner("test-token");
        assertNotNull(owner);
        assertEquals(1L, owner.getTenantId());
        assertEquals(100L, owner.getUserId());
        assertEquals(hex64, owner.getIdHash());
    }

    @Test
    public void testGetTrustedOwner_rejectsNonPositiveTenantUserOrMalformedHash() throws Exception {
        inject(guard, "redis", redis);
        when(redis.opsForValue()).thenReturn(ops);

        String hex64 = "a".repeat(64);

        // 1. tenantId <= 0
        when(ops.get(anyString())).thenReturn("0:100:" + hex64);
        assertNull(guard.getTrustedOwner("token"));

        when(ops.get(anyString())).thenReturn("-1:100:" + hex64);
        assertNull(guard.getTrustedOwner("token"));

        // 2. userId <= 0
        when(ops.get(anyString())).thenReturn("1:0:" + hex64);
        assertNull(guard.getTrustedOwner("token"));

        // 3. hash 长度不等于 64
        when(ops.get(anyString())).thenReturn("1:100:short-hash");
        assertNull(guard.getTrustedOwner("token"));

        // 4. hash 包含非法非 hex 字符
        when(ops.get(anyString())).thenReturn("1:100:" + "z".repeat(64));
        assertNull(guard.getTrustedOwner("token"));

        // 5. 格式错误
        when(ops.get(anyString())).thenReturn("1:100");
        assertNull(guard.getTrustedOwner("token"));
        when(ops.get(anyString())).thenReturn("abc:def:ghi");
        assertNull(guard.getTrustedOwner("token"));
    }
}
