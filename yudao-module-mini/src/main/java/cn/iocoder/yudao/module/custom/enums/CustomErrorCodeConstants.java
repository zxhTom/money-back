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

    /** 注册缺少邀请码 */
    ErrorCode INVITE_CODE_REQUIRED = new ErrorCode(10009, "请填写邀请码");

    /** 邀请码无效（不存在/已过期/已失效/邀请人未开启邀请功能） */
    ErrorCode INVITE_CODE_INVALID = new ErrorCode(10010, "邀请码无效或已过期");

    /** 当前用户未开启邀请功能，无法生成邀请码 */
    ErrorCode INVITE_NOT_ENABLED = new ErrorCode(10011, "当前账号未开启邀请功能");

    /** 注册过于频繁，触发风控 */
    ErrorCode REGISTER_TOO_FREQUENT = new ErrorCode(10012, "注册过于频繁，请稍后再试");

    /** 发起人脸认证需登录 */
    ErrorCode FACE_AUTH_NEED_LOGIN = new ErrorCode(10013, "请先登录再发起人脸认证");

    /** 只能对本人身份证发起人脸认证 */
    ErrorCode FACE_AUTH_IDCARD_NOT_SELF = new ErrorCode(10014, "只能对本人身份证发起人脸认证");

    /** 未完成实名认证，禁止访问 */
    ErrorCode USER_NOT_REAL_NAME_VERIFIED = new ErrorCode(10015, "请先完成实名认证后再使用");

    /** 信用查询必须提供身份证 */
    ErrorCode CONTRACT_QUERY_NEED_IDCARD = new ErrorCode(10016, "请提供身份证号进行查询");

    /** 信用查询过于频繁 */
    ErrorCode CONTRACT_QUERY_TOO_FREQUENT = new ErrorCode(10017, "请勿频繁访问");

    /** 皮肤配置不存在 */
    ErrorCode SKIN_PROFILE_NOT_EXISTS = new ErrorCode(10018, "皮肤配置不存在");

    /** 预设皮肤不允许修改核心配置（tokens/customCssText 等） */
    ErrorCode SKIN_PROFILE_PRESET_CANNOT_MODIFY = new ErrorCode(10019, "预设皮肤不允许修改核心配置");

    /** 预设皮肤或当前生效皮肤不允许删除 */
    ErrorCode SKIN_PROFILE_CANNOT_DELETE = new ErrorCode(10020, "预设皮肤或当前生效皮肤不允许删除");

    /** 自定义 CSS 文本格式非法 */
    ErrorCode SKIN_PROFILE_CSS_TEXT_INVALID = new ErrorCode(10021, "自定义CSS格式非法，仅支持 --token: value; 形式的声明");

    /** 文案套不存在 */
    ErrorCode TEXT_PROFILE_NOT_EXISTS = new ErrorCode(10022, "文案套不存在");

    /** 当前生效的文案套不允许删除 */
    ErrorCode TEXT_PROFILE_ACTIVE_CANNOT_DELETE = new ErrorCode(10023, "当前生效的文案套不允许删除");

    /** 图标集配置不存在 */
    ErrorCode ICON_SET_PROFILE_NOT_EXISTS = new ErrorCode(10024, "图标集配置不存在");

    /** 预设图标集不允许修改核心配置（icons） */
    ErrorCode ICON_SET_PROFILE_PRESET_CANNOT_MODIFY = new ErrorCode(10025, "预设图标集不允许修改核心配置");

    /** 预设图标集或当前生效图标集不允许删除 */
    ErrorCode ICON_SET_PROFILE_CANNOT_DELETE = new ErrorCode(10026, "预设图标集或当前生效图标集不允许删除");

    /** 图标值格式非法 */
    ErrorCode ICON_SET_PROFILE_SVG_INVALID = new ErrorCode(10027, "图标值格式非法，须为完整<svg>标签(不含script/事件)、http(s)图片URL、或以/开头的小程序本地路径");

}
