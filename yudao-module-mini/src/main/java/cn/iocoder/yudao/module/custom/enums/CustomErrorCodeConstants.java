package cn.iocoder.yudao.module.custom.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

public interface CustomErrorCodeConstants {

    ErrorCode CONTRACT_NOT_EXISTS = new ErrorCode(10001, "合同不存在");

    /** 微信模板消息发送失败（如 create 接口内公众号通知） */
    ErrorCode WECHAT_TEMPLATE_SEND_FAIL = new ErrorCode(10002, "微信模板消息发送失败: {}");

    /** 根据身份信息未找到对应用户，无法发送微信模板消息 */
    ErrorCode WECHAT_TEMPLATE_USER_NOT_FOUND = new ErrorCode(10003, "根据身份信息未找到对应用户，无法发送模板消息: {}");

    /** 时间窗口重合时长超过允许阈值，不允许保存 */
    ErrorCode TIME_WINDOW_OVERLAP_EXCEEDED = new ErrorCode(10004, "时间窗口与已有记录重合约 {} 小时，超过允许的最大重合时长 {} 小时");

    /** 时间窗口不存在 */
    ErrorCode TIME_WINDOW_NOT_EXISTS = new ErrorCode(10005, "时间窗口不存在");

    /** 设备模块开放配置不存在 */
    ErrorCode DEVICE_MODULE_ACCESS_NOT_EXISTS = new ErrorCode(10006, "设备模块开放配置不存在");

    /** 同一模块名与设备 ID 组合已被其它记录占用 */
    ErrorCode DEVICE_MODULE_ACCESS_MODULE_DEVICE_DUPLICATE = new ErrorCode(10007, "模块与设备组合已存在");

    /** 欠款人与债权人为同一人（身份证号相同） */
    ErrorCode CONTRACT_PARTY_SAME = new ErrorCode(10008, "欠款人与债权人不能为同一人，请核对双方身份证号");

}
