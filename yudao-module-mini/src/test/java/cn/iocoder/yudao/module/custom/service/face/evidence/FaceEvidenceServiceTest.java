package cn.iocoder.yudao.module.custom.service.face.evidence;

import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.custom.dal.dataobject.face.FaceEvidenceDO;
import cn.iocoder.yudao.module.custom.dal.mysql.face.FaceEvidenceMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FaceEvidenceServiceTest {

    @Mock
    private FaceEvidenceMapper faceEvidenceMapper;

    @InjectMocks
    private FaceEvidenceServiceImpl faceEvidenceService;

    private static void inject(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    private void login(Long userId, Long tenantId) {
        if (userId != null) {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(new LoginUser().setId(userId), null));
        } else {
            SecurityContextHolder.clearContext();
        }
        TenantContextHolder.setTenantId(tenantId);
    }

    @AfterEach
    public void cleanup() {
        SecurityContextHolder.clearContext();
        TenantContextHolder.clear();
    }

    @Test
    public void testSaveEvidence_successAndBoundedLength() {
        byte[] payload = new byte[100];
        String b64 = Base64.getEncoder().encodeToString(payload);

        when(faceEvidenceMapper.selectMetadataByTenantSessionAndMedia(eq(1L), anyString(), eq("best_image")))
                .thenReturn(null);

        FaceEvidenceDO res = faceEvidenceService.saveEvidence(1L, 100L, "token-1", "best_image", b64);
        assertNotNull(res);
        assertEquals(100L, res.getUserId());
        assertEquals(1L, res.getTenantId());
        assertEquals("best_image", res.getMediaType());
        assertTrue(res.getPath().startsWith("private/face-evidence/"));
        assertTrue(res.getPath().endsWith(".jpg"));
        verify(faceEvidenceMapper).insert(any(FaceEvidenceDO.class));
    }

    @Test
    public void testSaveEvidence_rejectsOversizeBeforeAndAfterBase64() {
        // 视频超出 8 MiB (8 * 1024 * 1024 + 1)
        byte[] large = new byte[8 * 1024 * 1024 + 1];
        String largeB64 = Base64.getEncoder().encodeToString(large);
        assertNull(faceEvidenceService.saveEvidence(1L, 100L, "token-1", "video", largeB64));
        verify(faceEvidenceMapper, never()).insert(any(FaceEvidenceDO.class));

        // 图片超出 2 MiB
        byte[] largePic = new byte[2 * 1024 * 1024 + 1];
        String largePicB64 = Base64.getEncoder().encodeToString(largePic);
        assertNull(faceEvidenceService.saveEvidence(1L, 100L, "token-1", "best_image", largePicB64));
        verify(faceEvidenceMapper, never()).insert(any(FaceEvidenceDO.class));

        // 畸形 Base64
        assertNull(faceEvidenceService.saveEvidence(1L, 100L, "token-1", "video", "invalid-base64-%%%"));
    }

    @Test
    public void testSaveEvidence_idempotentReturnsExistingWithoutInsert() {
        byte[] payload = new byte[50];
        String b64 = Base64.getEncoder().encodeToString(payload);

        FaceEvidenceDO existing = new FaceEvidenceDO();
        existing.setId(999L);
        when(faceEvidenceMapper.selectMetadataByTenantSessionAndMedia(eq(1L), anyString(), eq("video")))
                .thenReturn(existing);

        FaceEvidenceDO res = faceEvidenceService.saveEvidence(1L, 100L, "token-1", "video", b64);
        assertNotNull(res);
        assertEquals(999L, res.getId());
        verify(faceEvidenceMapper, never()).insert(any(FaceEvidenceDO.class));
    }

    @Test
    public void testSaveEvidence_concurrentDuplicateKeyGracefullyReturnsExisting() {
        byte[] payload = new byte[100];
        String b64 = Base64.getEncoder().encodeToString(payload);

        doThrow(new org.springframework.dao.DuplicateKeyException("Duplicate entry"))
                .when(faceEvidenceMapper).insert(any(FaceEvidenceDO.class));

        FaceEvidenceDO existingFromConcurrent = new FaceEvidenceDO();
        existingFromConcurrent.setId(888L);
        when(faceEvidenceMapper.selectMetadataByTenantSessionAndMedia(eq(1L), anyString(), eq("best_image")))
                .thenReturn(null)
                .thenReturn(existingFromConcurrent);

        FaceEvidenceDO res = faceEvidenceService.saveEvidence(1L, 100L, "token-dup", "best_image", b64);
        assertNotNull(res);
        assertEquals(888L, res.getId());
    }

    @Test
    public void testDownload_authorizedOwnerSuccess() {
        login(100L, 1L);

        FaceEvidenceDO meta = new FaceEvidenceDO();
        meta.setId(10L);
        meta.setTenantId(1L);
        meta.setUserId(100L);
        when(faceEvidenceMapper.selectMetadataById(10L)).thenReturn(meta);

        FaceEvidenceDO full = new FaceEvidenceDO();
        full.setId(10L);
        full.setTenantId(1L);
        full.setUserId(100L);
        full.setContent(new byte[]{1, 2, 3});
        when(faceEvidenceMapper.selectById(10L)).thenReturn(full);

        FaceEvidenceDO result = faceEvidenceService.getAuthorizedEvidence(10L);
        assertNotNull(result);
        assertArrayEquals(new byte[]{1, 2, 3}, result.getContent());
    }

    @Test
    public void testDownload_deniesAnonymousCrossUserCrossTenantAndDoesNotReadBlob() {
        // 1. 匿名
        login(null, 1L);
        assertThrows(AccessDeniedException.class, () -> faceEvidenceService.getAuthorizedEvidence(10L));
        verify(faceEvidenceMapper, never()).selectMetadataById(anyLong());
        verify(faceEvidenceMapper, never()).selectById(anyLong());

        // 2. 跨人
        login(101L, 1L);
        FaceEvidenceDO meta = new FaceEvidenceDO();
        meta.setId(10L);
        meta.setTenantId(1L);
        meta.setUserId(100L);
        when(faceEvidenceMapper.selectMetadataById(10L)).thenReturn(meta);
        assertThrows(AccessDeniedException.class, () -> faceEvidenceService.getAuthorizedEvidence(10L));
        verify(faceEvidenceMapper, never()).selectById(anyLong());

        // 3. 跨租户
        login(100L, 2L);
        assertThrows(AccessDeniedException.class, () -> faceEvidenceService.getAuthorizedEvidence(10L));
        verify(faceEvidenceMapper, never()).selectById(anyLong());

        // 4. 缺少有效租户
        login(100L, null);
        assertThrows(AccessDeniedException.class, () -> faceEvidenceService.getAuthorizedEvidence(10L));
        verify(faceEvidenceMapper, never()).selectById(anyLong());

        // 5. 不存在
        login(100L, 1L);
        when(faceEvidenceMapper.selectMetadataById(999L)).thenReturn(null);
        assertThrows(AccessDeniedException.class, () -> faceEvidenceService.getAuthorizedEvidence(999L));
        verify(faceEvidenceMapper, never()).selectById(999L);
    }
}
