package cn.iocoder.yudao.module.system.controller.admin.monitor.vo;

import lombok.Data;

import java.util.List;

@Data
public class IpDiagnoseResultVO {

    private String ip;

    /** true=有风险 */
    private boolean risky;

    /** 风险标签，如 [PROXY, VPN] */
    private List<String> risks;

    /** IP 类型，如 datacenter / residential / mobile */
    private String type;

    /** 国家 */
    private String country;

    /** 城市 */
    private String city;

    /** 运营商/服务商 */
    private String carrier;

    /** IP 数据来源（登录日志/操作日志/数据访问日志） */
    private List<String> sources;

    /** 结果来自缓存 */
    private boolean cached;

    /** 检测失败时的错误信息 */
    private String error;
}
