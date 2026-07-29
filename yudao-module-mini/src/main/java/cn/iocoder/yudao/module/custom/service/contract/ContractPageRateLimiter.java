package cn.iocoder.yudao.module.custom.service.contract;

import cn.iocoder.yudao.module.system.dal.dataobject.monitor.AlertRuleDO;
import cn.iocoder.yudao.module.system.service.monitor.AlertRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.custom.enums.CustomErrorCodeConstants.CONTRACT_QUERY_TOO_FREQUENT;

/**
 * 信用查询 /custom/contract/page 的多时间窗口限速（按登录用户维度）。
 *
 * 支持 3/5/10/15/30/60 分钟六个窗口各自的最大访问次数，任一窗口超限即拒绝，返回"请勿频繁访问"。
 * 全部可配（yudao.contract-page-rate-limit.m3/m5/m10/m15/m30/m60），不配用默认值；某窗口设为 0 表示不限该窗口。
 */
@Component
@Slf4j
public class ContractPageRateLimiter {

    @Value("${yudao.contract-page-rate-limit.m3:20}")
    private int m3;
    @Value("${yudao.contract-page-rate-limit.m5:30}")
    private int m5;
    @Value("${yudao.contract-page-rate-limit.m10:50}")
    private int m10;
    @Value("${yudao.contract-page-rate-limit.m15:70}")
    private int m15;
    @Value("${yudao.contract-page-rate-limit.m30:120}")
    private int m30;
    @Value("${yudao.contract-page-rate-limit.m60:200}")
    private int m60;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private AlertRuleService alertRuleService;

    /** 每个元素 [窗口秒数, 最大次数] */
    private List<int[]> windows;

    @PostConstruct
    public void init() {
        windows = new ArrayList<>();
        windows.add(new int[]{180, m3});
        windows.add(new int[]{300, m5});
        windows.add(new int[]{600, m10});
        windows.add(new int[]{900, m15});
        windows.add(new int[]{1800, m30});
        windows.add(new int[]{3600, m60});
    }

    public void check(Long userId) {
        if (userId == null) {
            return;
        }
        for (int[] w : windows) {
            int windowSec = w[0];
            int limit = w[1];
            if (limit <= 0) {
                continue; // 该窗口不限
            }
            String key = "contract:page:rl:" + userId + ":" + windowSec;
            Long count = stringRedisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                stringRedisTemplate.expire(key, windowSec, TimeUnit.SECONDS);
            }
            if (count != null && count > limit) {
                log.warn("[ContractPageRateLimit] userId={} {}秒窗口内访问 {} 次，超过上限 {}，拒绝", userId, windowSec, count, limit);
                AlertRuleDO rule = alertRuleService.getCachedRule("CONTRACT_QUERY_RATE_LIMIT");
                boolean exposeReason = rule != null && Integer.valueOf(1).equals(rule.getExposeReason());
                if (exposeReason) {
                    throw cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0(
                            CONTRACT_QUERY_TOO_FREQUENT.getCode(),
                            "查询过于频繁，{}秒内已达{}次，超过上限{}次，请稍后再试", windowSec, count, limit);
                }
                throw exception(CONTRACT_QUERY_TOO_FREQUENT);
            }
        }
    }
}
