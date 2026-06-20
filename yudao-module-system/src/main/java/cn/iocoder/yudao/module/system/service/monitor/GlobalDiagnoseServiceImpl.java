package cn.iocoder.yudao.module.system.service.monitor;

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
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class GlobalDiagnoseServiceImpl implements GlobalDiagnoseService {

    private static final String SCAN_RESULT_KEY      = "diagnose:scan:result:%d";
    private static final String BEHAVIOR_RESULT_KEY  = "diagnose:behavior:result";
    private static final String SCAN_RUNNING_KEY     = "diagnose:scan:running:%d";
    private static final String BEHAVIOR_RUNNING_KEY = "diagnose:behavior:running";

    @Resource private GlobalDiagnoseAsyncTask asyncTask;
    @Resource private StringRedisTemplate     stringRedisTemplate;

    // ─── 同步接口（原有，供其他服务直接调用）────────────────────

    @Override
    public List<GlobalIpDiagnoseVO> scanAllIps(int days) {
        return asyncTask.scanAllIps(days);
    }

    @Override
    public List<UserBehaviorVO> analyzeUserBehavior() {
        return asyncTask.analyzeUserBehavior();
    }

    // ─── 触发接口（带缓存判断，前端专用）────────────────────────

    @Override
    public Map<String, Object> triggerScanAllIps(int days) {
        String resultKey  = String.format(SCAN_RESULT_KEY, days);
        String runningKey = String.format(SCAN_RUNNING_KEY, days);

        String cached = stringRedisTemplate.opsForValue().get(resultKey);
        if (cached != null) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "cached");
            result.put("data", JSON.parseArray(cached, GlobalIpDiagnoseVO.class));
            return result;
        }

        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(runningKey, "1", 600, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(locked)) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "processing");
            return result;
        }

        asyncTask.doScanAllIpsAsync(days, resultKey, runningKey);

        Map<String, Object> result = new HashMap<>();
        result.put("status", "processing");
        return result;
    }

    @Override
    public Map<String, Object> triggerAnalyzeUserBehavior() {
        String cached = stringRedisTemplate.opsForValue().get(BEHAVIOR_RESULT_KEY);
        if (cached != null) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "cached");
            result.put("data", JSON.parseArray(cached, UserBehaviorVO.class));
            return result;
        }

        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(BEHAVIOR_RUNNING_KEY, "1", 300, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(locked)) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "processing");
            return result;
        }

        asyncTask.doAnalyzeBehaviorAsync(BEHAVIOR_RESULT_KEY, BEHAVIOR_RUNNING_KEY);

        Map<String, Object> result = new HashMap<>();
        result.put("status", "processing");
        return result;
    }
}
