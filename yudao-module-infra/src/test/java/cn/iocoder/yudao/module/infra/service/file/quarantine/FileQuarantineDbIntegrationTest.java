package cn.iocoder.yudao.module.infra.service.file.quarantine;

import cn.hutool.crypto.digest.DigestUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.quarantine.FileQuarantineDO;
import cn.iocoder.yudao.module.infra.dal.mysql.file.quarantine.FileQuarantineMapper;
import cn.iocoder.yudao.module.infra.service.file.FileConfigService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@Import({FileQuarantineServiceImpl.class})
public class FileQuarantineDbIntegrationTest extends BaseDbUnitTest {

    @Resource
    private FileQuarantineMapper mapper;

    @Resource
    private FileQuarantineServiceImpl service;

    @MockBean
    private FileConfigService fileConfigService;

    @Test
    public void testDatabaseExactByteMatching_caseAndTrailingSpacesAndSpecialChars() {
        // 插入隔离对象 history/face%25.mp4
        String path1 = "history/face%25.mp4";
        mapper.insert(FileQuarantineDO.builder()
                .configId(10L)
                .pathHash(DigestUtil.sha256Hex(path1.getBytes(StandardCharsets.UTF_8)))
                .canonicalPath(path1.getBytes(StandardCharsets.UTF_8))
                .reason("测试隔离")
                .build());

        // 插入尾部带空格的对象
        String path2 = "history/face.mp4 ";
        mapper.insert(FileQuarantineDO.builder()
                .configId(10L)
                .pathHash(DigestUtil.sha256Hex(path2.getBytes(StandardCharsets.UTF_8)))
                .canonicalPath(path2.getBytes(StandardCharsets.UTF_8))
                .reason("测试带空格隔离")
                .build());

        // 插入含问号对象
        String path3 = "history/face?.mp4";
        mapper.insert(FileQuarantineDO.builder()
                .configId(10L)
                .pathHash(DigestUtil.sha256Hex(path3.getBytes(StandardCharsets.UTF_8)))
                .canonicalPath(path3.getBytes(StandardCharsets.UTF_8))
                .reason("测试带问号隔离")
                .build());

        // 1. 精确匹配验证
        assertTrue(service.isQuarantined(10L, "history/face%25.mp4"));
        assertTrue(service.isQuarantined(10L, "history/face.mp4 "));
        assertTrue(service.isQuarantined(10L, "history/face?.mp4"));

        // 2. 字节级区分：无空格的对象不能误判为已隔离
        assertFalse(service.isQuarantined(10L, "history/face.mp4"));

        // 3. 大小写精确区分：History/face%25.mp4 不能误判为已隔离
        assertFalse(service.isQuarantined(10L, "History/face%25.mp4"));

        // 4. 不同 configId 不命中
        assertFalse(service.isQuarantined(20L, "history/face%25.mp4"));
    }
}
