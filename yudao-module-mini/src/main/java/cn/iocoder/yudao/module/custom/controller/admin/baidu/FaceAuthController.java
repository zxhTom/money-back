package cn.iocoder.yudao.module.custom.controller.admin.baidu;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.custom.controller.admin.baidu.vo.BaiduUserInfo;
import cn.iocoder.yudao.module.custom.framework.faceauth.FaceAuthCallbackTokenStore;
import cn.iocoder.yudao.module.custom.service.face.baidu.BaiduFaceAuthService;
import cn.iocoder.yudao.module.custom.service.wechat.WechatService;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.framework.idcard.IdCardCipherService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import com.anji.captcha.util.StringUtils;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.module.custom.enums.CustomErrorCodeConstants.FACE_AUTH_IDCARD_NOT_SELF;
import static cn.iocoder.yudao.module.custom.enums.CustomErrorCodeConstants.FACE_AUTH_NEED_LOGIN;


@RestController
@RequestMapping("/api/faceAuth")
public class FaceAuthController {

    @Autowired
    private BaiduFaceAuthService baiduFaceAuthService;

    @Autowired
    WechatService wechatService;

    @Autowired
    private IdCardCipherService idCardCipherService;
    @Resource
    private AdminUserService adminUserService;
    @Resource
    private FaceAuthCallbackTokenStore faceAuthCallbackTokenStore;
    // 你的小程序服务器域名，用于构造回调地址
    @Value("${server.domain}")
    private String serverDomain;

    /**
     * 第一步：小程序调用此接口，开启一个核身流程
     * 返回 verify_token 和 构造好的核身URL
     */
    @GetMapping("/start")
    public CommonResult<Map<String, Object>> startFaceAuth(
            @Parameter(description = "身份证：明文或密文（与 profile.idNo 一致），服务端自动识别") @RequestParam String idCard) throws Exception {
        // 注意：brain.toms.chat 核身完成后重定向到 successUrl/failUrl 时，只回传 status+idCard，
        // 不会透传我们追加的任何自定义参数（verify_token 都拿不回来），因此回调不能直接信任 status；
        // 服务端权威校验走 idCard -> verify_token 的映射（见 FaceAuthCallbackTokenStore）+ 主动查百度结果。
        String idCardEnc = URLEncoder.encode(idCard, StandardCharsets.UTF_8.name());
        String successUrl = serverDomain + "/api/mini/callback?status=success&idCard=" + idCardEnc;
        String failUrl = serverDomain + "/api/mini/callback?status=failed&idCard=" + idCardEnc;
        // 1. 获取 verify_token
        String verifyToken = baiduFaceAuthService.getVerifyToken(successUrl, failUrl);

        // 1.1 记录 idCard -> verify_token 映射，供回调时反查百度权威结果（防伪造 status）
        faceAuthCallbackTokenStore.store(idCard, verifyToken);

        // 2. 根据 token 构造H5核身页面URL[citation:6]
        String authUrl = String.format("https://brain.toms.chat/face/print/?token=%s&successUrl=%s&failedUrl=%s",
                verifyToken, successUrl, failUrl);

        Map<String, Object> result = new HashMap<>();
        result.put("verify_token", verifyToken);
        result.put("auth_url", authUrl); // 小程序端用 web-view 加载此链接
        return success(result);
    }

    /**
     * 第二步：小程序在获取用户输入的姓名、身份证后，调用此接口上报信息
     * 注意：这一步只是把姓名+身份证上报给百度做身份核验，不代表核身完成，
     * 不能在这里把 verified 置 1（此前的 bug：曾在这里无条件写库，导致"无论人脸核验成不成功都已认证"）。
     */
    @PostMapping("/reportInfo")
    public CommonResult<Map<String, Object>> reportUserInfo(@RequestBody BaiduUserInfo baiduUserInfo) throws Exception {
        String verifyToken = baiduUserInfo.getVerifyToken();
        if (StringUtils.isEmpty(verifyToken)) {
            CommonResult<Map<String, Object>> mapCommonResult = startFaceAuth(baiduUserInfo.getIdCard());
            Map<String, Object> checkedData = mapCommonResult.getCheckedData();
            verifyToken = checkedData.getOrDefault("verify_token","").toString();
        }
        String name = baiduUserInfo.getName();
        String idCard = idCardCipherService.decryptForExternalApi(baiduUserInfo.getIdCard());
        boolean success = baiduFaceAuthService.reportUserInfo(verifyToken, name, idCard);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        if (!success) {
            result.put("message", "信息上报失败，请重试");
        }
        return success(result);
    }

    // 注意：还需要一个查询最终核验结果的接口（调用百度云结果查询API[citation:2]）
    // 核身完成后，你的服务端必须调用此接口验证，不能仅依赖前端回调
    @PostMapping("/queryResult")
    public CommonResult<Map<String, Object>> queryFaceAuthResult(@RequestParam String verifyToken) throws Exception {
        return success(baiduFaceAuthService.queryFaceAuthResult(verifyToken));
    }

    // 真正生效的百度回调地址是 MiniCallbackController（/api/mini/callback），这里的 /callback 是未接线的遗留代码，已删除。
}
