package cn.iocoder.yudao.module.system.service.monitor;

import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.GlobalIpDiagnoseVO;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.UserBehaviorVO;

import java.util.List;

public interface GlobalDiagnoseService {

    /**
     * 扫描近 N 天内出现的所有 IP，关联用户、时间、来源，并检测风险。
     * 已缓存的 IP 直接返回缓存结果；未缓存的逐一调用外部接口（最多 MAX_UNCACHED 个）。
     */
    List<GlobalIpDiagnoseVO> scanAllIps(int days);

    /**
     * 分析近 30 天所有用户的登录行为：登录频次、失败次数、多 IP、活跃度、异常 IP。
     */
    List<UserBehaviorVO> analyzeUserBehavior();
}
