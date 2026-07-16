package cn.iocoder.yudao.module.custom.service.contract;

import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * 蜜罐合同数据生成器。
 *
 * 当检测到 IDOR 或合同不存在的非法访问时，返回与真实数据外观一致的伪造合同，
 * 迷惑攻击者并配合 warn 日志追踪来源。
 *
 * 生成规则：以 contractId 为随机种子，确保同一 ID 始终返回相同假数据，避免攻击者通过重复请求发现规律。
 * 假身份证号格式合法（通过加权校验位计算），但对应号码不存在于本系统任何用户中。
 */
public class ContractHoneypotFactory {

    public static final String HONEYPOT_CREATOR_MARKER = "__HONEYPOT__";

    private static final String[] SURNAMES = {
            "张", "王", "李", "赵", "陈", "刘", "杨", "黄", "吴", "周",
            "徐", "孙", "胡", "朱", "高", "林", "何", "郭", "马", "罗"
    };
    private static final String[] GIVEN_FEMALE = {
            "芳", "娜", "静", "丽", "英", "秀娟", "晓燕", "文静", "玉兰", "雪梅",
            "月华", "春花", "慧敏", "佳琳", "若冰", "梦洁", "思雨", "晨曦", "紫萱", "诗涵"
    };
    private static final String[] GIVEN_MALE = {
            "伟", "强", "磊", "军", "勇", "建国", "建华", "志远", "文杰", "宏伟",
            "天明", "国强", "昊天", "俊豪", "博文", "志刚", "明宇", "建平", "海涛", "嘉诚"
    };
    private static final String[] REGION_CODES = {
            "110101", "110102", "310101", "310104", "440103", "440105",
            "330102", "330105", "510104", "510105", "420102", "420104"
    };

    // 身份证加权位
    private static final int[] WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    private static final char[] CHECK_CHARS = "10X98765432".toCharArray();

    static ContractDO generate(Long requestedId) {
        // 种子固定 → 同 ID 同数据
        Random rng = new Random(requestedId * 2654435769L ^ 0xCAFEBABEDEADL);
        return generateWithRandom(rng, requestedId, HONEYPOT_CREATOR_MARKER);
    }

    /**
     * 供 {@link ContractTeaseFactory} 复用同一套"外观逼真的假合同"生成逻辑，
     * 只是种子来源和 creator 标记不同（戏耍数据不打蜜罐标记，避免污染安全监控面板）。
     */
    static ContractDO generateWithRandom(Random rng, Long requestedId, String creatorMarker) {
        ContractDO fake = new ContractDO();
        fake.setId(requestedId);
        fake.setIndebtedName(randomName(rng, true));
        fake.setCreditorName(randomName(rng, false));
        fake.setIndebtedId(generateIdCard(rng));
        fake.setCreditorId(generateIdCard(rng));

        long salaryBase = 5000L + ((long) rng.nextInt(990001));
        // 四舍五入到百位，更像真实金额
        salaryBase = (salaryBase / 100) * 100;
        fake.setSalary(salaryBase);

        long daysAgo = 30 + rng.nextInt(700);
        LocalDateTime created = LocalDateTime.now().minusDays(daysAgo);
        fake.setStartDate(created);
        fake.setEndDate(created.plusMonths(6 + rng.nextInt(19)));
        fake.setCreateTime(created);

        fake.setStatus(1);
        fake.setReturnType("一次性还款");
        fake.setReasonType("个人借款");
        fake.setDetailReason("临时周转");
        fake.setTariff(3L + rng.nextInt(10));
        fake.setInterest(0.0);
        fake.setRefund(0.0);

        // 内部标记，供 Controller 识别并记录额外安全日志；最终不对外暴露
        fake.setCreator(creatorMarker);

        return fake;
    }

    private static String randomName(Random rng, boolean femaleBias) {
        String surname = SURNAMES[rng.nextInt(SURNAMES.length)];
        String[] pool = (rng.nextInt(10) < (femaleBias ? 6 : 4)) ? GIVEN_FEMALE : GIVEN_MALE;
        return surname + pool[rng.nextInt(pool.length)];
    }

    // 生成格式合法、校验位正确的假身份证号
    private static String generateIdCard(Random rng) {
        String region = REGION_CODES[rng.nextInt(REGION_CODES.length)];
        int year = 1960 + rng.nextInt(41);
        int month = 1 + rng.nextInt(12);
        int day = 1 + rng.nextInt(28);
        String seq = String.format("%03d", 1 + rng.nextInt(999));
        String id17 = region + String.format("%04d%02d%02d", year, month, day) + seq;

        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += (id17.charAt(i) - '0') * WEIGHTS[i];
        }
        return id17 + CHECK_CHARS[sum % 11];
    }
}
