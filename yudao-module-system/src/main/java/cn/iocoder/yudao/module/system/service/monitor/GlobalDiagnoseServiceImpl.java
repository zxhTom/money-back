package cn.iocoder.yudao.module.system.service.monitor;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.GlobalIpDiagnoseVO;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.UserBehaviorVO;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class GlobalDiagnoseServiceImpl implements GlobalDiagnoseService {

    private static final String V2_SCAN_RESULT_KEY      = "diagnose:v2:scan:result:%d:%d:%d";
    private static final String V2_BEHAVIOR_RESULT_KEY  = "diagnose:v2:behavior:result:%d:%d";
    private static final String V2_SCAN_RUNNING_KEY     = "diagnose:v2:scan:running:%d:%d:%d";
    private static final String V2_BEHAVIOR_RUNNING_KEY = "diagnose:v2:behavior:running:%d:%d";

    @Resource private GlobalDiagnoseAsyncTask asyncTask;
    @Resource private StringRedisTemplate stringRedisTemplate;
    @Resource private GlobalDiagnoseAccessGuard diagnoseAccessGuard;

    @Override
    public List<GlobalIpDiagnoseVO> scanAllIps(int days) {
        GlobalDiagnoseTaskContext context = diagnoseAccessGuard.requireValidContext("scan-ips", days, null);
        return asyncTask.scanAllIps(context.getTenantId(), days);
    }

    @Override
    public List<UserBehaviorVO> analyzeUserBehavior() {
        GlobalDiagnoseTaskContext context = diagnoseAccessGuard.requireValidContext("user-behavior", null, null);
        return asyncTask.analyzeUserBehavior(context.getTenantId());
    }

    @Override
    public Map<String, Object> triggerScanAllIps(int days) {
        String lockToken = IdUtil.fastSimpleUUID();
        GlobalDiagnoseTaskContext context = diagnoseAccessGuard.requireValidContext("scan-ips", days, lockToken);
        Long tenantId = context.getTenantId();
        Long userId = context.getUserId();

        String resultKey = String.format(V2_SCAN_RESULT_KEY, tenantId, userId, days);
        String runningKey = String.format(V2_SCAN_RUNNING_KEY, tenantId, userId, days);

        String cached = stringRedisTemplate.opsForValue().get(resultKey);
        if (cached != null) {
            GlobalDiagnoseCacheEnvelope envelope = parseAndValidateEnvelope(cached, tenantId, userId, "scan-ips", days);
            if (envelope != null && envelope.getDataJson() != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("status", "cached");
                result.put("data", JSON.parseArray(envelope.getDataJson(), GlobalIpDiagnoseVO.class));
                return result;
            }
        }

        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(runningKey, lockToken, 600, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(locked)) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "processing");
            return result;
        }

        asyncTask.doScanAllIpsAsync(context, resultKey, runningKey);

        Map<String, Object> result = new HashMap<>();
        result.put("status", "processing");
        return result;
    }

    @Override
    public Map<String, Object> triggerAnalyzeUserBehavior() {
        String lockToken = IdUtil.fastSimpleUUID();
        GlobalDiagnoseTaskContext context = diagnoseAccessGuard.requireValidContext("user-behavior", null, lockToken);
        Long tenantId = context.getTenantId();
        Long userId = context.getUserId();

        String resultKey = String.format(V2_BEHAVIOR_RESULT_KEY, tenantId, userId);
        String runningKey = String.format(V2_BEHAVIOR_RUNNING_KEY, tenantId, userId);

        String cached = stringRedisTemplate.opsForValue().get(resultKey);
        if (cached != null) {
            GlobalDiagnoseCacheEnvelope envelope = parseAndValidateEnvelope(cached, tenantId, userId, "user-behavior", null);
            if (envelope != null && envelope.getDataJson() != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("status", "cached");
                result.put("data", JSON.parseArray(envelope.getDataJson(), UserBehaviorVO.class));
                return result;
            }
        }

        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(runningKey, lockToken, 300, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(locked)) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "processing");
            return result;
        }

        asyncTask.doAnalyzeBehaviorAsync(context, resultKey, runningKey);

        Map<String, Object> result = new HashMap<>();
        result.put("status", "processing");
        return result;
    }

    private GlobalDiagnoseCacheEnvelope parseAndValidateEnvelope(String raw, Long expectedTenantId, Long expectedUserId, String expectedTaskType, Integer expectedDays) {
        try {
            GlobalDiagnoseCacheEnvelope envelope = JSON.parseObject(raw, GlobalDiagnoseCacheEnvelope.class);
            if (envelope == null) return null;
            if (!Objects.equals(expectedTenantId, envelope.getTenantId())) return null;
            if (!Objects.equals(expectedUserId, envelope.getUserId())) return null;
            if (!Objects.equals(expectedTaskType, envelope.getTaskType())) return null;
            if (!Objects.equals(expectedDays, envelope.getDays())) return null;
            return envelope;
        } catch (Exception e) {
            return null;
        }
    }
}
