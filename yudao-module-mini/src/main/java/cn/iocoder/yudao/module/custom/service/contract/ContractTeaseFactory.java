package cn.iocoder.yudao.module.custom.service.contract;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractPageReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.custom.vo.CreditPageReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.custom.vo.CreditSearchVO;
import cn.iocoder.yudao.module.custom.controller.admin.custom.vo.DimensionCombineRespVo;
import cn.iocoder.yudao.module.custom.controller.admin.custom.vo.RecentContractVO;
import cn.iocoder.yudao.module.custom.controller.admin.custom.vo.StaticsContractPeriodRespVO;
import cn.iocoder.yudao.module.custom.controller.admin.custom.vo.TotalInfosRespVO;
import cn.iocoder.yudao.module.custom.controller.admin.custom.vo.UserDimensionChartRespVO;
import cn.iocoder.yudao.module.custom.controller.admin.custom.vo.UserDimensionRespVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * "戏耍模式"假数据生成器。
 *
 * 与 {@link ContractHoneypotFactory} 复用同一套"外观逼真"的假合同生成逻辑，
 * 但种子额外混入 loginUserId：
 *  - 保证同一个被戏耍用户、同样的查询参数，每次得到完全相同的假数据（不能被反复请求发现规律）；
 *  - 保证两个不同的被戏耍账号不会看到一模一样的假数据、互相印证发现异常。
 *
 * 不使用 {@link ContractHoneypotFactory#HONEYPOT_CREATOR_MARKER} 标记——
 * 戏耍账号只是被限制查看真实数据，不是攻击者，不应污染安全监控面板上的蜜罐命中统计。
 */
public class ContractTeaseFactory {

    private static final String[] STATUS_NAMES = {"正常", "逾期", "已结清", "催收中"};
    private static final String[] CODE_POOL = {"A", "B", "C", "D", "E"};

    private ContractTeaseFactory() {
    }

    // ------------------------------------------------------------------
    // 种子构造：loginUserId + 场景标识 + 具体查询参数（依赖 toString，保证同参数确定性一致）
    // ------------------------------------------------------------------
    private static long buildSeed(Long loginUserId, Object... parts) {
        StringBuilder sb = new StringBuilder();
        sb.append(loginUserId);
        for (Object part : parts) {
            sb.append('|').append(part);
        }
        long hash = 0;
        String s = sb.toString();
        for (int i = 0; i < s.length(); i++) {
            hash = hash * 31 + s.charAt(i);
        }
        return hash ^ 0x5DEECE66DL;
    }

    // ------------------------------------------------------------------
    // 单条合同（getContract）
    // ------------------------------------------------------------------
    public static ContractDO generateContract(Long requestedId, Long loginUserId) {
        Random rng = new Random(buildSeed(loginUserId, "contract", requestedId));
        return ContractHoneypotFactory.generateWithRandom(rng, requestedId, null);
    }

    // ------------------------------------------------------------------
    // 合同分页（getContractPageForListing / getSelfContractPage / getContractPage 导出等）
    // ------------------------------------------------------------------
    public static PageResult<ContractDO> generateContractPage(Long loginUserId, ContractPageReqVO pageReqVO) {
        long seed = buildSeed(loginUserId, "contract-page", pageReqVO);
        Random rng = new Random(seed);
        int pageSize = pageReqVO.getPageSize() == null ? 10 : pageReqVO.getPageSize();
        int pageNo = pageReqVO.getPageNo() == null ? 1 : pageReqVO.getPageNo();
        long total = 3 + rng.nextInt(80);
        int rowsOnThisPage = (int) Math.max(0, Math.min(pageSize, total - (long) (pageNo - 1) * pageSize));
        List<ContractDO> list = generateContractList(rng, seed, rowsOnThisPage);
        return new PageResult<>(list, total);
    }

    // ------------------------------------------------------------------
    // 合同列表（无分页信封，CustomDefineServiceImpl.page()）
    // ------------------------------------------------------------------
    public static List<ContractDO> generateContractList(Long loginUserId, Object paramsKey, int maxCount) {
        long seed = buildSeed(loginUserId, "contract-list", paramsKey);
        Random rng = new Random(seed);
        int count = maxCount <= 0 ? 0 : rng.nextInt(maxCount + 1);
        return generateContractList(rng, seed, count);
    }

    private static List<ContractDO> generateContractList(Random rng, long seed, int count) {
        List<ContractDO> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            long fakeId = 900000L + Math.abs(rng.nextLong() % 90000L);
            Random itemRng = new Random(seed ^ fakeId ^ i);
            list.add(ContractHoneypotFactory.generateWithRandom(itemRng, fakeId, null));
        }
        return list;
    }

    // ------------------------------------------------------------------
    // 信用查询分页（creditSearch）
    // ------------------------------------------------------------------
    public static Page<CreditSearchVO> generateCreditSearchPage(Long loginUserId, CreditPageReqVO pageReqVO) {
        long seed = buildSeed(loginUserId, "credit-search", pageReqVO);
        Random rng = new Random(seed);
        int pageSize = pageReqVO.getPageSize() == null ? 10 : pageReqVO.getPageSize();
        int pageNo = pageReqVO.getPageNo() == null ? 1 : pageReqVO.getPageNo();
        Page<CreditSearchVO> page = new Page<>(pageNo, pageSize);
        int count = 1 + rng.nextInt(4);
        List<CreditSearchVO> records = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            CreditSearchVO vo = new CreditSearchVO();
            vo.setCode(CODE_POOL[rng.nextInt(CODE_POOL.length)]);
            vo.setStatusName(STATUS_NAMES[rng.nextInt(STATUS_NAMES.length)]);
            vo.setCount((long) (1 + rng.nextInt(20)));
            vo.setSalary(1000.0 + rng.nextInt(50000));
            records.add(vo);
        }
        page.setRecords(records);
        page.setTotal(count);
        return page;
    }

    // ------------------------------------------------------------------
    // 最近合同列表（rencentContractList）—— idNo 留空，跳过调用方的证件号加解密逻辑
    // ------------------------------------------------------------------
    public static List<RecentContractVO> generateRecentContractList(Long loginUserId) {
        long seed = buildSeed(loginUserId, "recent-contract");
        Random rng = new Random(seed);
        int count = rng.nextInt(4);
        List<RecentContractVO> list = new ArrayList<>();
        String[] surnames = {"张", "王", "李", "赵", "陈"};
        String[] givens = {"伟", "芳", "静", "磊", "娜"};
        for (int i = 0; i < count; i++) {
            RecentContractVO vo = new RecentContractVO();
            vo.setName(surnames[rng.nextInt(surnames.length)] + givens[rng.nextInt(givens.length)]);
            vo.setAvatarUrl(null);
            list.add(vo);
        }
        return list;
    }

    // ------------------------------------------------------------------
    // 时段统计仪表盘（staticsContractByTimePeriod）
    // ------------------------------------------------------------------
    public static StaticsContractPeriodRespVO generateStaticsContractPeriod(Long loginUserId) {
        long seed = buildSeed(loginUserId, "statics-period", LocalDate.now());
        Random rng = new Random(seed);
        StaticsContractPeriodRespVO vo = new StaticsContractPeriodRespVO();
        vo.setTodayCount((long) (1 + rng.nextInt(20)));
        vo.setTodayRevenue(BigDecimal.valueOf(1000 + rng.nextInt(50000)));
        vo.setMonthCount(vo.getTodayCount() + rng.nextInt(200));
        vo.setMonthRevenue(vo.getTodayRevenue().add(BigDecimal.valueOf(rng.nextInt(500000))));
        vo.setTotalCount(vo.getMonthCount() + rng.nextInt(2000));
        vo.setTotalRevenue(vo.getMonthRevenue().add(BigDecimal.valueOf(rng.nextInt(2000000))));
        vo.setAvgSalary(BigDecimal.valueOf(5000 + rng.nextInt(20000)));
        vo.setTodayAvgSalary(BigDecimal.valueOf(5000 + rng.nextInt(20000)));
        vo.setStatDate(LocalDate.now());
        vo.setMonthStartDate(LocalDate.now().withDayOfMonth(1));
        return vo;
    }

    // ------------------------------------------------------------------
    // 用户维度仪表盘（userDimension）
    // ------------------------------------------------------------------
    public static DimensionCombineRespVo generateUserDimension(Long loginUserId) {
        long seed = buildSeed(loginUserId, "user-dimension", LocalDate.now());
        Random rng = new Random(seed);

        UserDimensionRespVO data = new UserDimensionRespVO();
        data.setKeyNames("本月");
        data.setActiveUsers(100L + rng.nextInt(5000));
        data.setNewUsers(10L + rng.nextInt(500));
        data.setVipUsers(5L + rng.nextInt(200));

        UserDimensionChartRespVO chart = new UserDimensionChartRespVO();
        List<String> months = new ArrayList<>();
        List<Long> active = new ArrayList<>();
        List<Long> newUsers = new ArrayList<>();
        List<Long> vipUsers = new ArrayList<>();
        LocalDate cursor = LocalDate.now().minusMonths(5);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        for (int i = 0; i < 6; i++) {
            months.add(cursor.format(formatter));
            active.add(100L + rng.nextInt(5000));
            newUsers.add(10L + rng.nextInt(500));
            vipUsers.add(5L + rng.nextInt(200));
            cursor = cursor.plusMonths(1);
        }
        chart.setKeyNames(months);
        chart.setActiveUsers(active);
        chart.setNewUsers(newUsers);
        chart.setVipUsers(vipUsers);

        DimensionCombineRespVo respVo = new DimensionCombineRespVo();
        respVo.setData(data);
        respVo.setChart(chart);
        return respVo;
    }

    // ------------------------------------------------------------------
    // 合计信息（totalInfos）
    // ------------------------------------------------------------------
    public static TotalInfosRespVO generateTotalInfos(Long loginUserId, ContractPageReqVO reqVO) {
        long seed = buildSeed(loginUserId, "total-infos", reqVO);
        Random rng = new Random(seed);
        TotalInfosRespVO vo = new TotalInfosRespVO();
        vo.setTotalSalary(1000.0 + rng.nextInt(500000));
        return vo;
    }

}
