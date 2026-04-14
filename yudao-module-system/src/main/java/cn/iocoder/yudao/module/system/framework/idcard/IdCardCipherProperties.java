package cn.iocoder.yudao.module.system.framework.idcard;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 身份证加解密与解密接口限流等配置（密钥须与迁移脚本 / 运维约定一致）
 */
@Data
@ConfigurationProperties(prefix = "yudao.id-card")
public class IdCardCipherProperties {

    /**
     * 密钥与加解密算法参数
     */
    private Cipher cipher = new Cipher();

    /**
     * 历史明文数据迁移开关（仅一次性任务）
     */
    private Migration migration = new Migration();

    /**
     * 保留配置节；明文/密文由服务端按形态自动识别，见 {@link cn.iocoder.yudao.module.system.framework.idcard.IdCardCipherService}
     */
    private Request request = new Request();

    /**
     * 解密接口限流（按登录用户维度，依赖 Redis）
     */
    private DecryptApi decryptApi = new DecryptApi();

    /**
     * 对外展示字段 idNoDisplay 等（与链路透传密文字段 idNo 区分）
     */
    private Display display = new Display();

    @Data
    public static class Cipher {
        /**
         * 至少 8 位；生产环境请使用 32 位以上随机串并独立保管
         */
        private String secret = "dev-only-change-in-yaml-at-least-32-chars-long-key";
    }

    @Data
    public static class Migration {
        private boolean enabled = false;
    }

    @Data
    public static class Request {
        /**
         * 已废弃：不再参与逻辑。明文通过 15/18 位形态识别，其余按密文解密。
         */
        @Deprecated
        private boolean allowPlain = false;
    }

    @Data
    public static class DecryptApi {
        /**
         * 是否启用「每用户每窗口请求次数」限制
         */
        private boolean rateLimitEnabled = true;
        /**
         * 统计窗口时长（秒），窗口内调用解密接口次数不能超过 rate-max-requests
         */
        private int rateWindowSeconds = 60;
        /**
         * 每个 rate-window 内允许调用解密接口的最大次数（同一用户）
         */
        private int rateMaxRequests = 10;

        /**
         * 是否启用「每用户每统计周期内不同密文个数」限制
         */
        private boolean distinctLimitEnabled = true;
        /**
         * 统计周期（秒），例如 86400 表示按自然滑动窗口近似为「每天」桶
         */
        private int distinctWindowSeconds = 86400;
        /**
         * 每个 distinct-window 内，允许解密的不同密文（证件）最大种数
         */
        private int distinctMaxPerWindow = 20;
    }

    @Data
    public static class Display {
        /**
         * false：链路透传字段（如 idNo、债权人/债务人证件）为密文；idNoDisplay 等为脱敏（前 6 + * + 后 4）<br>
         * true：上述 idNo 类字段与 idNoDisplay 均为完整明文（生产慎用）
         */
        private boolean plainForIdNoDisplay = false;
    }
}
