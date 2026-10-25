package cn.iocoder.yudao.module.infra.framework.file.core.s3;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.util.validation.ValidationUtils;
import cn.iocoder.yudao.module.infra.framework.file.core.client.s3.S3FileClient;
import cn.iocoder.yudao.module.infra.framework.file.core.client.s3.S3FileClientConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.validation.Validation;

@SuppressWarnings("resource")
public class S3FileClientTest {

    @Test
    public void testExtractStorageKey_defaultDomainAndPresignKeyExact() {
        S3FileClientConfig config = new S3FileClientConfig();
        config.setAccessKey("admin");
        config.setAccessSecret("password");
        config.setBucket("evidence-bucket");
        config.setDomain(null);
        config.setEndpoint("http://127.0.0.1:9000");
        config.setEnablePathStyleAccess(true);
        config.setEnablePublicAccess(false);

        S3FileClient client = new S3FileClient(10L, config);
        client.init();

        // 真实 client 提取 storageKey
        String extracted = client.extractStorageKey("http://127.0.0.1:9000/evidence-bucket/history/face.mp4");
        org.junit.jupiter.api.Assertions.assertEquals("history/face.mp4", extracted);

        // 包含 literal % 的普通文件正常保留
        String percentKey = client.extractStorageKey("http://127.0.0.1:9000/evidence-bucket/docs/report_100%_123.pdf");
        org.junit.jupiter.api.Assertions.assertEquals("docs/report_100%_123.pdf", percentKey);

        // 包含 literal %25, ?, #, +, 尾部空格 的对象正常精确提取，不截断也不被 trim 掉尾空格
        org.junit.jupiter.api.Assertions.assertEquals("history/face%25.mp4",
                client.extractStorageKey("http://127.0.0.1:9000/evidence-bucket/history/face%25.mp4"));
        org.junit.jupiter.api.Assertions.assertEquals("history/face?.mp4",
                client.extractStorageKey("history/face?.mp4"));
        org.junit.jupiter.api.Assertions.assertEquals("history/face#1.mp4",
                client.extractStorageKey("history/face#1.mp4"));
        org.junit.jupiter.api.Assertions.assertEquals("history/face+tag.mp4",
                client.extractStorageKey("http://127.0.0.1:9000/evidence-bucket/history/face+tag.mp4"));
        org.junit.jupiter.api.Assertions.assertEquals("history/face.mp4 ",
                client.extractStorageKey("history/face.mp4 "));

        // presignGetUrl 正常生成且 key 一致
        String presigned = client.presignGetUrl("http://127.0.0.1:9000/evidence-bucket/history/face.mp4", 60);
        org.junit.jupiter.api.Assertions.assertNotNull(presigned);
        org.junit.jupiter.api.Assertions.assertTrue(presigned.contains("/evidence-bucket/history/face.mp4"));
        org.junit.jupiter.api.Assertions.assertTrue(presigned.contains("X-Amz-Signature="));
    }

    @Test
    @Disabled // MinIO，如果要集成测试，可以注释本行
    public void testMinIO() throws Exception {
        S3FileClientConfig config = new S3FileClientConfig();
        // 配置成你自己的
        config.setAccessKey("admin");
        config.setAccessSecret("password");
        config.setBucket("yudaoyuanma");
        config.setDomain(null);
        // 默认 9000 endpoint
        config.setEndpoint("http://127.0.0.1:9000");

        // 执行上传
        testExecuteUpload(config);
    }

    @Test
    @Disabled // 阿里云 OSS，如果要集成测试，可以注释本行
    public void testAliyun() throws Exception {
        S3FileClientConfig config = new S3FileClientConfig();
        // 配置成你自己的
        config.setAccessKey(System.getenv("ALIYUN_ACCESS_KEY"));
        config.setAccessSecret(System.getenv("ALIYUN_SECRET_KEY"));
        config.setBucket("yunai-aoteman");
        config.setDomain(null); // 如果有自定义域名，则可以设置。http://ali-oss.iocoder.cn
        // 默认北京的 endpoint
        config.setEndpoint("oss-cn-beijing.aliyuncs.com");

        // 执行上传
        testExecuteUpload(config);
    }

    @Test
    @Disabled // 腾讯云 COS，如果要集成测试，可以注释本行
    public void testQCloud() throws Exception {
        S3FileClientConfig config = new S3FileClientConfig();
        // 配置成你自己的
        config.setAccessKey(System.getenv("QCLOUD_ACCESS_KEY"));
        config.setAccessSecret(System.getenv("QCLOUD_SECRET_KEY"));
        config.setBucket("aoteman-1255880240");
        config.setDomain(null); // 如果有自定义域名，则可以设置。http://tengxun-oss.iocoder.cn
        // 默认上海的 endpoint
        config.setEndpoint("cos.ap-shanghai.myqcloud.com");

        // 执行上传
        testExecuteUpload(config);
    }

    @Test
    @Disabled // 七牛云存储，如果要集成测试，可以注释本行
    public void testQiniu() throws Exception {
        S3FileClientConfig config = new S3FileClientConfig();
        // 配置成你自己的
//        config.setAccessKey(System.getenv("QINIU_ACCESS_KEY"));
//        config.setAccessSecret(System.getenv("QINIU_SECRET_KEY"));
        config.setAccessKey("b7yvuhBSAGjmtPhMFcn9iMOxUOY_I06cA_p0ZUx8");
        config.setAccessSecret("kXM1l5ia1RvSX3QaOEcwI3RLz3Y2rmNszWonKZtP");
        config.setBucket("ruoyi-vue-pro");
        config.setDomain("http://test.yudao.iocoder.cn"); // 如果有自定义域名，则可以设置。http://static.yudao.iocoder.cn
        config.setEnablePathStyleAccess(false);
        // 默认上海的 endpoint
        config.setEndpoint("s3-cn-south-1.qiniucs.com");

        // 执行上传
        testExecuteUpload(config);
    }

    @Test
    @Disabled // 七牛云存储（读私有桶），如果要集成测试，可以注释本行
    public void testQiniu_privateGet() {
        S3FileClientConfig config = new S3FileClientConfig();
        // 配置成你自己的
//        config.setAccessKey(System.getenv("QINIU_ACCESS_KEY"));
//        config.setAccessSecret(System.getenv("QINIU_SECRET_KEY"));
        config.setAccessKey("b7yvuhBSAGjmtPhMFcn9iMOxUOY_I06cA_p0ZUx8");
        config.setAccessSecret("kXM1l5ia1RvSX3QaOEcwI3RLz3Y2rmNszWonKZtP");
        config.setBucket("ruoyi-vue-pro-private");
        config.setDomain("http://t151glocd.hn-bkt.clouddn.com"); // 如果有自定义域名，则可以设置。http://static.yudao.iocoder.cn
        config.setEnablePathStyleAccess(false);
        // 默认上海的 endpoint
        config.setEndpoint("s3-cn-south-1.qiniucs.com");

        // 校验配置
        ValidationUtils.validate(Validation.buildDefaultValidatorFactory().getValidator(), config);
        // 创建 Client
        S3FileClient client = new S3FileClient(0L, config);
        client.init();
        // 执行生成 URL 签名
        String path = "output.png";
        String presignedUrl = client.presignGetUrl(path, 300);
        System.out.println(presignedUrl);
    }

    @Test
    @Disabled // 华为云存储，如果要集成测试，可以注释本行
    public void testHuaweiCloud() throws Exception {
        S3FileClientConfig config = new S3FileClientConfig();
        // 配置成你自己的
//        config.setAccessKey(System.getenv("HUAWEI_CLOUD_ACCESS_KEY"));
//        config.setAccessSecret(System.getenv("HUAWEI_CLOUD_SECRET_KEY"));
        config.setBucket("yudao");
        config.setDomain(null); // 如果有自定义域名，则可以设置。
        // 默认上海的 endpoint
        config.setEndpoint("obs.cn-east-3.myhuaweicloud.com");

        // 执行上传
        testExecuteUpload(config);
    }

    private void testExecuteUpload(S3FileClientConfig config) {
        // 校验配置
        ValidationUtils.validate(Validation.buildDefaultValidatorFactory().getValidator(), config);
        // 创建 Client
        S3FileClient client = new S3FileClient(0L, config);
        client.init();
        // 上传文件
        String path = IdUtil.fastSimpleUUID() + ".jpg";
        byte[] content = ResourceUtil.readBytes("file/erweima.jpg");
        String fullPath = client.upload(content, path, "image/jpeg");
        System.out.println("访问地址：" + fullPath);
        // 读取文件
        if (true) {
            byte[] bytes = client.getContent(path);
            System.out.println("文件内容：" + bytes.length);
        }
        // 删除文件
        if (false) {
            client.delete(path);
        }
    }

}
