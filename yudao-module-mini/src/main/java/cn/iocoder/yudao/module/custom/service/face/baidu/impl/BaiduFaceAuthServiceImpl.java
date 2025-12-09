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

/**
 * @author zxhtom
 * 12/3/25
 */
@Service
public class BaiduFaceAuthServiceImpl implements BaiduFaceAuthService {

    // 从配置文件 application.yml 中读取
    @Value("${baidu.face.api-key}")
    private String apiKey;
    @Value("${baidu.face.secret-key}")
    private String secretKey;
    @Value("${baidu.face.plan-id}")
    private String planId;

    // 百度云获取 access_token 的地址 (需提前获取)
    private static final String ACCESS_TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=%s&client_secret=%s";
    // 获取 verify_token 的接口地址[citation:4]
    private static final String VERIFY_TOKEN_URL = "https://aip.baidubce.com/rpc/2.0/brain/solution/faceprint/verifyToken/generate";
    // 上报用户信息的接口地址[citation:4]
    private static final String REPORT_USER_INFO_URL = "https://aip.baidubce.com/rpc/2.0/brain/solution/faceprint/userInfo/report";

    private static final String REPORT_USER_ID_URL = "https://aip.baidubce.com/rpc/2.0/brain/solution/faceprint/idcard/submit";


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
    public Map<String, Object> queryFaceAuthResult(String verifyToken) throws Exception {
        // 调用百度云的人脸实名认证V4结果查询接口
        // 接口地址示例：https://aip.baidubce.com/rest/2.0/face/v4/facverify
        String url = "https://aip.baidubce.com/rest/2.0/face/v4/facverify";

        // 构建请求参数（根据百度云最新文档调整）
        Map<String, String> params = new HashMap<>();
        params.put("verify_token", verifyToken);
        // 可能还需要其他参数，如plan_id等

        // 获取access_token并发送请求
        String accessToken = getAccessToken(); // 复用之前的getAccessToken方法
        String resultStr = sendPost(url + "?access_token=" + accessToken,
                JSON.toJSONString(params));

        JSONObject result = JSON.parseObject(resultStr);

        Map<String, Object> response = new HashMap<>();

        // 解析百度云返回结果（结构需参照官方文档）
        if (result.getInteger("error_code") == 0) {
            JSONObject verifyResult = result.getJSONObject("result");
            response.put("success", true);
            response.put("data", verifyResult); // 包含passed、score、reason等
        } else {
            response.put("success", false);
            response.put("message", result.getString("error_msg"));
        }

        return response;
    }

    /**
     * 通用方法：获取百度云API的访问凭证 (access_token)
     */
    private String getAccessToken() throws Exception {
        String url = String.format(ACCESS_TOKEN_URL, apiKey, secretKey);
        String result = sendGet(url);
        JSONObject jsonObject = JSON.parseObject(result);
        return jsonObject.getString("access_token");
    }

    // 发送HTTP POST请求的辅助方法
    private String sendPost(String url, String body) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(body, "UTF-8"));
            HttpResponse response = httpClient.execute(post);
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        }
    }

    // 发送HTTP GET请求的辅助方法
    private String sendGet(String url) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(url);
            HttpResponse response = httpClient.execute(get);
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        }
    }
}
