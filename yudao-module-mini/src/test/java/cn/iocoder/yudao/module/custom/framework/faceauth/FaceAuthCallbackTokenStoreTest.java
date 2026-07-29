package cn.iocoder.yudao.module.custom.framework.faceauth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FaceAuthCallbackTokenStoreTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private FaceAuthCallbackTokenStore tokenStore;

    @BeforeEach
    public void setUp() {
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    public void testStore_writesWithThirtyMinuteTtl() {
        tokenStore.store("idCardPlain", "verifyTokenAbc");
        verify(valueOperations).set(eq("face:verify:token:by:idcard:idCardPlain"), eq("verifyTokenAbc"), eq(30L), eq(TimeUnit.MINUTES));
    }

    @Test
    public void testConsumeAndGet_returnsValueAndDeletesKey() {
        when(valueOperations.get("face:verify:token:by:idcard:idCardPlain")).thenReturn("verifyTokenAbc");

        String result = tokenStore.consumeAndGet("idCardPlain");

        assertEquals("verifyTokenAbc", result);
        verify(stringRedisTemplate).delete("face:verify:token:by:idcard:idCardPlain");
    }

    @Test
    public void testConsumeAndGet_missingKey_returnsNullAndDoesNotDelete() {
        when(valueOperations.get("face:verify:token:by:idcard:idCardPlain")).thenReturn(null);

        String result = tokenStore.consumeAndGet("idCardPlain");

        assertNull(result);
        verify(stringRedisTemplate, never()).delete(anyString());
    }
}
