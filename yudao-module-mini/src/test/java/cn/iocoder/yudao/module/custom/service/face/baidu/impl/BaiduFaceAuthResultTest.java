package cn.iocoder.yudao.module.custom.service.face.baidu.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BaiduFaceAuthResultTest {
    @Test
    void usesH5DetailEndpointAndNormalizesConfirmedSuccess() throws Exception {
        Stub service = new Stub("{\"success\":true,\"result\":{\"verify_result\":{\"score\":95},"
                + "\"idcard_confirm\":{\"idcard_number\":\"identity\",\"name\":\"测试\"}}}");
        Map<String, Object> response = service.queryFaceAuthResult("server-token");
        assertTrue(service.requestUrl.startsWith("https://aip.baidubce.com/rpc/2.0/brain/solution/faceprint/result/detail?"));
        assertEquals("server-token", JSON.parseObject(service.requestBody).getString("verify_token"));
        assertEquals(true, response.get("success"));
        JSONObject data = (JSONObject) response.get("data");
        assertEquals(true, data.getBoolean("passed"));
        assertEquals("identity", data.getJSONObject("idcard_confirm").getString("idcard_number"));
    }

    @Test
    void rejectsFailedOrMalformedProviderResults() throws Exception {
        for (String json : new String[]{"{\"success\":false,\"code\":283437,\"message\":\"expired\"}",
                "{\"success\":true,\"result\":null}", "{\"success\":true,\"result\":{}}",
                "{\"error_code\":0,\"result\":{\"passed\":true}}"}) {
            Map<String, Object> response = new Stub(json).queryFaceAuthResult("server-token");
            assertEquals(false, response.get("success"), json);
            assertNull(response.get("data"));
        }
    }

    private static class Stub extends BaiduFaceAuthServiceImpl {
        private final String response;
        private String requestUrl;
        private String requestBody;
        Stub(String response) { this.response = response; }
        @Override protected String sendGet(String url) { return "{\"access_token\":\"test-token\"}"; }
        @Override protected String sendPost(String url, String body) {
            requestUrl = url;
            requestBody = body;
            return response;
        }
    }
}
