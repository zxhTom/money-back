package cn.iocoder.yudao.module.custom.service.face.baidu.impl;


import cn.iocoder.yudao.module.custom.service.face.baidu.BaiduFaceAuthService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import cn.iocoder.yudao.module.custom.service.face.FaceAuthSessionGuard;
import cn.iocoder.yudao.module.custom.service.face.evidence.FaceEvidenceService;
import cn.iocoder.yudao.module.custom.dal.dataobject.face.FaceEvidenceDO;
import cn.iocoder.yudao.module.system.framework.idcard.IdCardCipherService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zxhtom
 * 12/3/25
 */
@Service
@Slf4j
public class BaiduFaceAuthServiceImpl implements BaiduFaceAuthService {

    // 从配置文件 application.yml 中读取
    @Value("${baidu.face.api-key}")
    private String apiKey;
    @Value("${baidu.face.secret-key}")
    private String secretKey;
    @Value("${baidu.face.plan-id}")
    private String planId;

    @Resource
    private FaceAuthSessionGuard faceAuthSessionGuard;

    @Resource
    private FaceEvidenceService faceEvidenceService;

    @Resource
    private IdCardCipherService idCardCipherService;

    // 百度云获取 access_token 的地址 (需提前获取)
    private static final String ACCESS_TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=%s&client_secret=%s";
    // 获取 verify_token 的接口地址[citation:4]
    private static final String VERIFY_TOKEN_URL = "https://aip.baidubce.com/rpc/2.0/brain/solution/faceprint/verifyToken/generate";
    // 上报用户信息的接口地址[citation:4]
    private static final String REPORT_USER_INFO_URL = "https://aip.baidubce.com/rpc/2.0/brain/solution/faceprint/userInfo/report";

    private static final String REPORT_USER_ID_URL = "https://aip.baidubce.com/rpc/2.0/brain/solution/faceprint/idcard/submit";
    private static final String QUERY_RESULT_URL = "https://aip.baidubce.com/rpc/2.0/brain/solution/faceprint/result/detail";


    /**
     * 1. 获取核身流程的 verify_token
     */
    @Override
    public String getVerifyToken(String successCallbackUrl, String failCallbackUrl) throws Exception {
        // 首先获取 access_token
        String accessToken = getAccessToken();

        // 构造请求参数[citation:4]
        Map<String, Object> params = new HashMap<>();
        params.put("plan_id", planId);
        Map<String, String> redirectConfig = new HashMap<>();
        redirectConfig.put("success_url", successCallbackUrl);
        redirectConfig.put("failed_url", failCallbackUrl);
        params.put("redirect_config", redirectConfig);
        // 注意：文档指出此处 access_token 参数可选，但通常通过URL传递[citation:4]
        // params.put("access_token", accessToken);

        String result = sendPost(VERIFY_TOKEN_URL + "?access_token=" + accessToken, JSON.toJSONString(params));
        JSONObject resultJson = JSON.parseObject(result);
        if (resultJson.getBoolean("success")) {
            return resultJson.getJSONObject("result").getString("verify_token");
        } else {
            throw new RuntimeException("获取verify_token失败: " + resultJson.getString("message"));
        }
    }

    /**
     * 2. 上报指定用户的姓名和身份证号[citation:4]
     * @param verifyToken 上一步获取的token
     * @param name 真实姓名
     * @param idCard 身份证号码
     */
    @Override
    public boolean reportUserInfo(String verifyToken, String name, String idCard) throws Exception {
        String accessToken = getAccessToken();

        Map<String, Object> params = new HashMap<>();
        params.put("verify_token", verifyToken);
        params.put("id_name", name); // 姓名参数为 id_name[citation:4]
        params.put("id_no", idCard); // 身份证参数为 id_no[citation:4]
        params.put("certificate_type", 0); // 0代表大陆居民二代身份证[citation:4]

        String result = sendPost(REPORT_USER_ID_URL + "?access_token=" + accessToken, JSON.toJSONString(params));
        JSONObject resultJson = JSON.parseObject(result);
        return resultJson.getBoolean("success"); // 成功返回 true[citation:4]
    }
    @Override
    public Map<String, Object> queryFaceAuthResult(String verifyToken) {
        // H5 方案的权威结果查询，不是单次人脸实名认证 V4 的图片核验接口。
        String url = QUERY_RESULT_URL;
        Map<String, Object> response = new HashMap<>();

        try {
            // 构建请求参数（根据百度云最新文档调整）
            Map<String, String> params = new HashMap<>();
            params.put("verify_token", verifyToken);

            // 获取 access_token 并发送请求（网络与 token 获取置于统一异常边界）
            String accessToken = getAccessToken();
            String resultStr = sendPost(url + "?access_token=" + accessToken,
                    JSON.toJSONString(params));

            JSONObject result;
            try {
                result = JSON.parseObject(resultStr);
                if (result == null) {
                    throw new IllegalArgumentException("Provider响应非JSON对象");
                }
            } catch (Exception e) {
                log.error("[FaceAuth] 解析核身提供商响应失败: error={}", e.getClass().getSimpleName());
                response.put("success", false);
                response.put("message", "核身提供商响应解析失败");
                return response;
            }

            // 解析百度云返回结果（结构需参照官方文档，脱敏异常与类型边界）
            Object successObj = result.get("success");
            boolean isSuccess = Boolean.TRUE.equals(successObj);
            JSONObject resultObj = result.getJSONObject("result");

            if (isSuccess
                    && resultObj != null
                    && resultObj.getJSONObject("verify_result") != null) {
                JSONObject verifyResult = resultObj;

                // 结构校验：若包含 idcard_confirm，必须是合法的 JSONObject 结构，否则进入外层固定安全失败
                String confirmedId = null;
                if (verifyResult.containsKey("idcard_confirm")) {
                    Object idConfirmObj = verifyResult.get("idcard_confirm");
                    if (!(idConfirmObj instanceof JSONObject)) {
                        throw new IllegalArgumentException("idcard_confirm 类型不合法");
                    }
                    JSONObject idcardConfirm = (JSONObject) idConfirmObj;
                    confirmedId = idcardConfirm.getString("idcard_number");
                }

                // passed 是内部统一字段，只能由已认证的百度 result/detail 响应派生。
                verifyResult.put("passed", true);
                response.put("success", true);
                response.put("data", verifyResult); // 包含passed、score、reason等

                // 提取媒体 base64 用于私有存证保存，随后无论类型是否合法彻底从返回数据中移除
                String videoBase64 = null;
                if (verifyResult.containsKey("video")) {
                    Object videoObj = verifyResult.remove("video");
                    if (videoObj instanceof String) {
                        videoBase64 = (String) videoObj;
                    }
                }

                String picBase64 = null;
                if (verifyResult.containsKey("best_image")) {
                    Object bestImageObj = verifyResult.remove("best_image");
                    if (bestImageObj instanceof JSONObject) {
                        JSONObject bestImage = (JSONObject) bestImageObj;
                        Object picObj = bestImage.get("pic");
                        if (picObj instanceof String) {
                            picBase64 = (String) picObj;
                        }
                    }
                }

                // 保存到独立私有存证库（根据可信 Redis 会话解析归属，并核验 provider 确认的证件号）
                FaceAuthSessionGuard.SessionOwner owner = null;
                try {
                    owner = faceAuthSessionGuard.getTrustedOwner(verifyToken);
                } catch (Exception e) {
                    log.error("[FaceAuth] 解析会话归属异常: {}", e.getClass().getSimpleName());
                }

                if (owner != null && confirmedId != null && faceAuthSessionGuard.matchesIdentity(owner, confirmedId)) {
                    try {
                        if (videoBase64 != null) {
                            FaceEvidenceDO videoEvidence = faceEvidenceService.saveEvidence(
                                    owner.getTenantId(), owner.getUserId(), verifyToken, "video", videoBase64);
                            if (videoEvidence != null) {
                                response.put("videoEvidenceId", videoEvidence.getId());
                                response.put("videoEvidenceUrl", "/admin-api/api/faceAuth/evidence/" + videoEvidence.getId());
                            }
                        }
                        if (picBase64 != null) {
                            FaceEvidenceDO picEvidence = faceEvidenceService.saveEvidence(
                                    owner.getTenantId(), owner.getUserId(), verifyToken, "best_image", picBase64);
                            if (picEvidence != null) {
                                response.put("picEvidenceId", picEvidence.getId());
                                response.put("picEvidenceUrl", "/admin-api/api/faceAuth/evidence/" + picEvidence.getId());
                            }
                        }
                    } catch (Exception e) {
                        // 不在日志中输出完整异常堆栈或消息，避免泄漏敏感凭证或身份；存证异常不破坏已认证结果
                        log.error("[FaceAuth] 保存存证异常: {}", e.getClass().getSimpleName());
                    }
                } else if (owner != null && confirmedId != null) {
                    log.warn("[FaceAuth] 提供商确认身份与可信会话不一致，拒绝本次认证结果");
                    response.put("success", false);
                    response.put("message", "核身身份与会话发起人不一致");
                    response.remove("data");
                    return response;
                } else {
                    log.warn("[FaceAuth] 会话归属不存在或与提供商确认身份不匹配，跳过存证保存");
                }

            } else {
                response.put("success", false);
                response.put("message", sanitizeProviderMessage(result.get("message")));
            }
        } catch (Exception e) {
            log.error("[FaceAuth] 处理核身结果异常: error={}", e.getClass().getSimpleName());
            response.clear();
            response.put("success", false);
            response.put("message", "核身提供商响应处理失败");
        }

        return response;
    }

    private static String sanitizeProviderMessage(Object msgObj) {
        if (!(msgObj instanceof String)) {
            return "核身认证未通过";
        }
        String msg = ((String) msgObj).trim();
        if (msg.isEmpty()) {
            return "核身认证未通过";
        }
        String lower = msg.toLowerCase();
        if (lower.contains("token") || lower.contains("expired") || lower.contains("expire")) {
            return "核身流程已失效或过期，请重新发起";
        }
        if (lower.contains("idcard") || lower.contains("id_no") || lower.contains("name")
                || lower.contains("param") || lower.contains("match") || lower.contains("fail")
                || lower.contains("not pass") || lower.contains("reject") || lower.contains("invalid")
                || lower.contains("score") || lower.contains("verify") || lower.contains("auth")) {
            return "核身信息核验不通过，请重试";
        }
        return "核身认证未通过";
    }

    /**
     * 通用方法：获取百度云API的访问凭证 (access_token)
     */
    protected String getAccessToken() throws Exception {
        String url = String.format(ACCESS_TOKEN_URL, apiKey, secretKey);
        String result = sendGet(url);
        JSONObject jsonObject = JSON.parseObject(result);
        return jsonObject.getString("access_token");
    }

    // 发送HTTP POST请求的辅助方法
    protected String sendPost(String url, String body) throws Exception {
        org.apache.http.client.config.RequestConfig timeout = org.apache.http.client.config.RequestConfig.custom()
                .setConnectTimeout(5000).setConnectionRequestTimeout(5000).setSocketTimeout(8000).build();
        try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(timeout).build()) {
            HttpPost post = new HttpPost(url);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(body, "UTF-8"));
            HttpResponse response = httpClient.execute(post);
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        }
    }

    // 发送HTTP GET请求的辅助方法
    protected String sendGet(String url) throws Exception {
        org.apache.http.client.config.RequestConfig timeout = org.apache.http.client.config.RequestConfig.custom()
                .setConnectTimeout(5000).setConnectionRequestTimeout(5000).setSocketTimeout(8000).build();
        try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(timeout).build()) {
            HttpGet get = new HttpGet(url);
            HttpResponse response = httpClient.execute(get);
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        }
    }
}
