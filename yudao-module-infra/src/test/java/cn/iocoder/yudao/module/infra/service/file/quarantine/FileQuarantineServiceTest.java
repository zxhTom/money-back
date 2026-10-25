package cn.iocoder.yudao.module.infra.service.file.quarantine;

import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileConfigDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.quarantine.FileQuarantineDO;
import cn.iocoder.yudao.module.infra.dal.mysql.file.quarantine.FileQuarantineMapper;
import cn.iocoder.yudao.module.infra.framework.file.core.client.s3.S3FileClientConfig;
import cn.iocoder.yudao.module.infra.service.file.FileConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.security.access.AccessDeniedException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileQuarantineServiceTest {

    @Mock
    private FileQuarantineMapper fileQuarantineMapper;

    @Mock
    private FileConfigService fileConfigService;

    @InjectMocks
    private FileQuarantineServiceImpl service;

    private static void inject(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    @BeforeEach
    public void setUp() throws Exception {
        inject(service, "fileConfigService", fileConfigService);
    }

    @Test
    public void testCanonicalizePath_normalAndEncoded() {
        assertEquals("avatar/1.png", service.canonicalizePath("avatar/1.png"));
        assertEquals("avatar/1.png", service.canonicalizePath("/avatar//1.png"));
        assertEquals("avatar/1.png", service.canonicalizePath("avatar%2F1.png"));
        assertEquals("avatar/1.png", service.canonicalizePath("avatar%252F1.png"));
        assertEquals("avatar/1.png", service.canonicalizePath("https://oss.example.com/admin-api/infra/file/1/get/avatar/1.png"));
        // 允许文件名中包含字面量 %（如 report_100%_123.pdf），不误封为恶意编码
        assertEquals("docs/report_100%_123.pdf", service.canonicalizePath("docs/report_100%_123.pdf"));
        // 允许混合编码与字面量百分号（如 docs/a%25+b%.pdf）
        assertEquals("docs/a%25+b%.pdf", service.canonicalizePath("docs/a%25+b%.pdf"));
        // 保留路径与 key 的末尾空格
        assertEquals("docs/file with space.pdf ", service.canonicalizePath("docs/file with space.pdf "));
    }

    @Test
    public void testCanonicalizePath_rejectsDotAndDotDotTraversal() {
        assertThrows(IllegalArgumentException.class, () -> service.canonicalizePath("public/../private/test.jpg"));
        assertThrows(IllegalArgumentException.class, () -> service.canonicalizePath("public/%2e%2e/private/test.jpg"));
        assertThrows(IllegalArgumentException.class, () -> service.canonicalizePath("public/%252e%252e/private/test.jpg"));
        assertThrows(IllegalArgumentException.class, () -> service.canonicalizePath("public/./test.jpg"));
        assertThrows(IllegalArgumentException.class, () -> service.canonicalizePath("public/%2e/test.jpg"));
    }

    @Test
    public void testIsPrivateFaceEvidence_identifiesVariantsAndRejectsTraversal() {
        assertTrue(service.isPrivateFaceEvidence("private/face-evidence/uuid.jpg"));
        assertTrue(service.isPrivateFaceEvidence("/private/face-evidence/uuid.mp4"));
        assertTrue(service.isPrivateFaceEvidence("private%2Fface-evidence%2Fuuid.mp4"));
        assertTrue(service.isPrivateFaceEvidence("https://example.com/admin-api/infra/file/1/get/private/face-evidence/uuid.jpg"));
        // 尝试穿越变体，按设计直接判定为私有或非法拒绝
        assertTrue(service.isPrivateFaceEvidence("public/../private/face-evidence/uuid.jpg"));
        assertFalse(service.isPrivateFaceEvidence("public/brand/logo.png"));
        assertFalse(service.isPrivateFaceEvidence("private/contracts/42/1.pdf"));
    }

    @Test
    public void testExtractStorageKey_handlesS3DomainPrefix() {
        FileConfigDO configDO = new FileConfigDO();
        S3FileClientConfig s3Config = new S3FileClientConfig();
        s3Config.setDomain("https://my-bucket.oss.com");
        s3Config.setBucket("my-bucket");
        configDO.setConfig(s3Config);
        when(fileConfigService.getFileConfig(10L)).thenReturn(configDO);

        assertEquals("my-bucket/avatar.png", service.extractStorageKey(10L, "https://my-bucket.oss.com/my-bucket/avatar.png"));
        assertEquals("my-bucket/avatar.png", service.extractStorageKey(10L, "my-bucket/avatar.png"));
        assertEquals("folder/sub/a.jpg", service.extractStorageKey(10L, "https://my-bucket.oss.com/folder/sub/a.jpg"));

        // 覆盖 S3Client 在 domain 为空但 endpoint/bucket 存在时与 S3FileClient 默认 buildDomain 逻辑一致
        FileConfigDO minioConfig = new FileConfigDO();
        S3FileClientConfig minioS3 = new S3FileClientConfig();
        minioS3.setDomain(null);
        minioS3.setEndpoint("http://127.0.0.1:9000");
        minioS3.setBucket("evidence-bucket");
        minioConfig.setConfig(minioS3);
        when(fileConfigService.getFileConfig(20L)).thenReturn(minioConfig);
        assertEquals("history/face.mp4", service.extractStorageKey(20L, "http://127.0.0.1:9000/evidence-bucket/history/face.mp4"));
    }

    @Test
    public void testIsQuarantined_exactMatchAndDatabaseFailureFailsClosed() {
        when(fileQuarantineMapper.existsByConfigAndCanonicalPath(1L, "history/face.mp4")).thenReturn(true);
        assertTrue(service.isQuarantined(1L, "history/face.mp4"));
        assertTrue(service.isQuarantined(1L, "/history//face.mp4"));

        // 字节级精确区分：大小写不同不能命中隔离，尾部空格不同也不能误判
        when(fileQuarantineMapper.existsByConfigAndCanonicalPath(1L, "History/face.mp4")).thenReturn(false);
        assertFalse(service.isQuarantined(1L, "History/face.mp4"));
        when(fileQuarantineMapper.existsByConfigAndCanonicalPath(1L, "history/face.mp4 ")).thenReturn(false);
        assertFalse(service.isQuarantined(1L, "history/face.mp4 "));

        // 特殊字符精确匹配支持：%25、?、#、+、连续斜杠
        when(fileQuarantineMapper.existsByConfigAndCanonicalPath(1L, "history/face%25.mp4")).thenReturn(true);
        assertTrue(service.isQuarantined(1L, "history/face%25.mp4"));

        when(fileQuarantineMapper.existsByConfigAndCanonicalPath(1L, "history/face?.mp4")).thenReturn(true);
        assertTrue(service.isQuarantined(1L, "history/face?.mp4"));

        when(fileQuarantineMapper.existsByConfigAndCanonicalPath(1L, "history/face#1.mp4")).thenReturn(true);
        assertTrue(service.isQuarantined(1L, "history/face#1.mp4"));

        when(fileQuarantineMapper.existsByConfigAndCanonicalPath(1L, "history/face+tag.mp4")).thenReturn(true);
        assertTrue(service.isQuarantined(1L, "history/face+tag.mp4"));

        when(fileQuarantineMapper.existsByConfigAndCanonicalPath(1L, "public/normal.png")).thenReturn(false);
        assertFalse(service.isQuarantined(1L, "public/normal.png"));

        // 数据库异常时 fail-closed，抛出 AccessDeniedException
        when(fileQuarantineMapper.existsByConfigAndCanonicalPath(eq(1L), eq("err.jpg")))
                .thenThrow(new DataAccessResourceFailureException("DB down"));
        assertThrows(AccessDeniedException.class, () -> service.isQuarantined(1L, "err.jpg"));
    }
}
