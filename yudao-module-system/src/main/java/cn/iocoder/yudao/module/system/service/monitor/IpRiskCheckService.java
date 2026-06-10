package cn.iocoder.yudao.module.system.service.monitor;

import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.IpDiagnoseResultVO;

import java.util.List;

public interface IpRiskCheckService {

    /**
     * 异步检测 IP 风险，发现异常则写入告警。
     * 检测结果缓存 1 小时，同一 IP 不重复调用外部接口。
     */
    void checkAsync(String ip, Long userId, String requestUrl);

    /**
     * 同步诊断用户所有历史 IP（登录日志 + 操作日志 + 数据访问日志）。
     * 每个 IP 单独调用检测接口，结果汇总返回。
     */
    List<IpDiagnoseResultVO> diagnoseUser(Long userId);
}
