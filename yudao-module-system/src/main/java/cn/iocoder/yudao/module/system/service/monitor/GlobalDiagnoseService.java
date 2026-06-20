package cn.iocoder.yudao.module.system.service.monitor;

import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.GlobalIpDiagnoseVO;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.UserBehaviorVO;

import java.util.List;
import java.util.Map;

public interface GlobalDiagnoseService {

    List<GlobalIpDiagnoseVO> scanAllIps(int days);

    List<UserBehaviorVO> analyzeUserBehavior();

    /**
     * 触发IP全局扫描。
     * 有缓存 → 立即返回 {status:"cached", data:[...]}
     * 无缓存 → 启动异步任务，返回 {status:"processing"}，完成后通过 WebSocket 推送结果
     */
    Map<String, Object> triggerScanAllIps(int days);

    /**
     * 触发用户行为分析，同上。
     */
    Map<String, Object> triggerAnalyzeUserBehavior();
}
