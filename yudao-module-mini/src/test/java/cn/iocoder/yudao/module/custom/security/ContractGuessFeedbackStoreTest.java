package cn.iocoder.yudao.module.custom.security;

import cn.iocoder.yudao.module.custom.service.contract.ContractGuessFeedbackStore;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ContractGuessFeedbackStoreTest {

    @Test
    @SuppressWarnings("unchecked")
    void externalTokenIsBoundToCallerAndDirectionAndWorksOnce() throws Exception {
        ContractGuessFeedbackStore store = new ContractGuessFeedbackStore();
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        Field field = store.getClass().getDeclaredField("redis");
        field.setAccessible(true);
        field.set(store, redis);
        ConcurrentHashMap<String, String> entries = new ConcurrentHashMap<>();
        doAnswer(call -> { entries.put(call.getArgument(0), call.getArgument(1)); return null; })
                .when(values).set(anyString(), anyString(), eq(30L), eq(TimeUnit.MINUTES));
        when(values.get(anyString())).thenAnswer(call -> entries.get(call.getArgument(0)));
        when(values.getAndDelete(anyString())).thenAnswer(call -> entries.remove(call.getArgument(0)));

        String token = store.createExternalToken(1L, 42L, "from", 13L);
        assertEquals(13L, store.peekExternalToken(1L, 42L, "from", token));
        assertEquals(13L, store.peekExternalToken(1L, 42L, "from", token));
        assertNull(store.peekExternalToken(1L, 99L, "from", token));
        assertNull(store.consumeExternalToken(1L, 99L, "from", token));
        assertNull(store.consumeExternalToken(1L, 42L, "to", token));
        assertEquals(13L, store.consumeExternalToken(1L, 42L, "from", token));
        assertNull(store.consumeExternalToken(1L, 42L, "from", token));
    }

    @Test
    @SuppressWarnings("unchecked")
    void onlyAnOfferedCandidateCanBeCountedOnce() throws Exception {
        ContractGuessFeedbackStore store = new ContractGuessFeedbackStore();
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        ZSetOperations<String, String> scores = mock(ZSetOperations.class);
        HashOperations<String, Object, Object> last = mock(HashOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        when(redis.opsForZSet()).thenReturn(scores);
        when(redis.opsForHash()).thenReturn(last);
        Field field = store.getClass().getDeclaredField("redis");
        field.setAccessible(true);
        field.set(store, redis);
        AtomicReference<String> offered = new AtomicReference<>();
        doAnswer(call -> { offered.set(call.getArgument(1)); return null; })
                .when(values).set(anyString(), anyString(), eq(30L), eq(TimeUnit.MINUTES));
        when(values.getAndDelete(anyString())).thenAnswer(call -> offered.getAndSet(null));

        String badBatch = store.createImpression(1L, 42L, "from", Collections.singletonMap(9L, "id-9"));
        assertFalse(store.recordSelection(1L, 42L, "from", badBatch, "other-id"));
        verify(scores, never()).incrementScore(anyString(), anyString(), anyDouble());

        String batch = store.createImpression(1L, 42L, "from", Collections.singletonMap(9L, "id-9"));
        assertTrue(store.recordSelection(1L, 42L, "from", batch, "id-9"));
        assertFalse(store.recordSelection(1L, 42L, "from", batch, "id-9"));
        verify(scores, times(1)).incrementScore(contains("choices:1:42:from"), eq("9"), eq(1.0));
        verify(last, times(1)).put(contains("choices:1:42:from:last"), eq("9"), anyString());
    }
}
