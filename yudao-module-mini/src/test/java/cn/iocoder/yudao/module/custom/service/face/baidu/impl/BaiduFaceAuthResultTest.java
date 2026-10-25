package cn.iocoder.yudao.module.custom.service.face.baidu.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
    void mediaFieldsAreStrippedAndEvidenceSavedPrivatelyWhenOwnerMatches() throws Exception {
        Stub service = new Stub("{\"success\":true,\"result\":{\"verify_result\":{\"score\":95},"
                + "\"idcard_confirm\":{\"idcard_number\":\"identity\",\"name\":\"测试\"},"
                + "\"video\":\"dmlkZW8tZGF0YQ==\","
                + "\"best_image\":{\"pic\":\"cGljLWRhdGE=\"}}}");
        cn.iocoder.yudao.module.custom.service.face.FaceAuthSessionGuard sessionGuard = mock(cn.iocoder.yudao.module.custom.service.face.FaceAuthSessionGuard.class);
        cn.iocoder.yudao.module.custom.service.face.evidence.FaceEvidenceService faceEvidenceService = mock(cn.iocoder.yudao.module.custom.service.face.evidence.FaceEvidenceService.class);
        cn.iocoder.yudao.module.custom.service.face.FaceAuthSessionGuard.SessionOwner owner =
                new cn.iocoder.yudao.module.custom.service.face.FaceAuthSessionGuard.SessionOwner(1L, 1024L, "a".repeat(64));
        when(sessionGuard.getTrustedOwner("server-token")).thenReturn(owner);
        when(sessionGuard.matchesIdentity(owner, "identity")).thenReturn(true);

        cn.iocoder.yudao.module.custom.dal.dataobject.face.FaceEvidenceDO vid = new cn.iocoder.yudao.module.custom.dal.dataobject.face.FaceEvidenceDO();
        vid.setId(101L);
        cn.iocoder.yudao.module.custom.dal.dataobject.face.FaceEvidenceDO pic = new cn.iocoder.yudao.module.custom.dal.dataobject.face.FaceEvidenceDO();
        pic.setId(102L);
        when(faceEvidenceService.saveEvidence(eq(1L), eq(1024L), eq("server-token"), eq("video"), anyString())).thenReturn(vid);
        when(faceEvidenceService.saveEvidence(eq(1L), eq(1024L), eq("server-token"), eq("best_image"), anyString())).thenReturn(pic);

        java.lang.reflect.Field f1 = BaiduFaceAuthServiceImpl.class.getDeclaredField("faceAuthSessionGuard");
        f1.setAccessible(true);
        f1.set(service, sessionGuard);
        java.lang.reflect.Field f2 = BaiduFaceAuthServiceImpl.class.getDeclaredField("faceEvidenceService");
        f2.setAccessible(true);
        f2.set(service, faceEvidenceService);

        Map<String, Object> response = service.queryFaceAuthResult("server-token");
        assertEquals(true, response.get("success"));
        JSONObject data = (JSONObject) response.get("data");
        assertFalse(data.containsKey("video"));
        if (data.containsKey("best_image")) {
            assertFalse(data.getJSONObject("best_image").containsKey("pic"));
        }
        assertNull(response.get("videoUrl"));
        assertNull(response.get("picUrl"));
        assertEquals(101L, response.get("videoEvidenceId"));
        assertEquals("/admin-api/api/faceAuth/evidence/101", response.get("videoEvidenceUrl"));
        assertEquals(102L, response.get("picEvidenceId"));
        assertEquals("/admin-api/api/faceAuth/evidence/102", response.get("picEvidenceUrl"));
    }

    @Test
    void mediaFieldsAreStrippedEvenIfBestImageIsMalformedOrOwnerDoesNotMatch() throws Exception {
        Stub service = new Stub("{\"success\":true,\"result\":{\"verify_result\":{\"score\":95},"
                + "\"idcard_confirm\":{\"idcard_number\":\"identity\",\"name\":\"测试\"},"
                + "\"video\":\"raw-video-base64\","
                + "\"best_image\":\"string-malformed-image\"}}");
        cn.iocoder.yudao.module.custom.service.face.FaceAuthSessionGuard sessionGuard = mock(cn.iocoder.yudao.module.custom.service.face.FaceAuthSessionGuard.class);
        cn.iocoder.yudao.module.custom.service.face.evidence.FaceEvidenceService faceEvidenceService = mock(cn.iocoder.yudao.module.custom.service.face.evidence.FaceEvidenceService.class);
        when(sessionGuard.getTrustedOwner(anyString())).thenReturn(null);

        java.lang.reflect.Field f1 = BaiduFaceAuthServiceImpl.class.getDeclaredField("faceAuthSessionGuard");
        f1.setAccessible(true);
        f1.set(service, sessionGuard);
        java.lang.reflect.Field f2 = BaiduFaceAuthServiceImpl.class.getDeclaredField("faceEvidenceService");
        f2.setAccessible(true);
        f2.set(service, faceEvidenceService);

        Map<String, Object> response = service.queryFaceAuthResult("server-token");
        assertEquals(true, response.get("success"));
        JSONObject data = (JSONObject) response.get("data");
        assertFalse(data.containsKey("video"));
        assertFalse(data.containsKey("best_image"));
        assertNull(response.get("videoUrl"));
        assertNull(response.get("picUrl"));
        verifyNoInteractions(faceEvidenceService);
    }

    @Test
    void providerIdentityMismatchRejectsEvidenceSaveAndReturnsServiceError() throws Exception {
        Stub service = new Stub("{\"success\":true,\"result\":{\"verify_result\":{\"score\":95},"
                + "\"idcard_confirm\":{\"idcard_number\":\"provider-id-999\",\"name\":\"冒用者\"},"
                + "\"video\":\"dmlkZW8tZGF0YQ==\","
                + "\"best_image\":{\"pic\":\"cGljLWRhdGE=\"}}}");
        cn.iocoder.yudao.module.custom.service.face.FaceAuthSessionGuard sessionGuard = mock(cn.iocoder.yudao.module.custom.service.face.FaceAuthSessionGuard.class);
        cn.iocoder.yudao.module.custom.service.face.evidence.FaceEvidenceService faceEvidenceService = mock(cn.iocoder.yudao.module.custom.service.face.evidence.FaceEvidenceService.class);
        cn.iocoder.yudao.module.custom.service.face.FaceAuthSessionGuard.SessionOwner owner =
                new cn.iocoder.yudao.module.custom.service.face.FaceAuthSessionGuard.SessionOwner(1L, 1024L, "a".repeat(64));
        when(sessionGuard.getTrustedOwner("server-token")).thenReturn(owner);
        // Provider 返回的身份与可信 Session 身份不匹配
        when(sessionGuard.matchesIdentity(owner, "provider-id-999")).thenReturn(false);

        java.lang.reflect.Field f1 = BaiduFaceAuthServiceImpl.class.getDeclaredField("faceAuthSessionGuard");
        f1.setAccessible(true);
        f1.set(service, sessionGuard);
        java.lang.reflect.Field f2 = BaiduFaceAuthServiceImpl.class.getDeclaredField("faceEvidenceService");
        f2.setAccessible(true);
        f2.set(service, faceEvidenceService);

        Map<String, Object> response = service.queryFaceAuthResult("server-token");
        assertEquals(false, response.get("success"));
        assertEquals("核身身份与会话发起人不一致", response.get("message"));
        assertNull(response.get("data"));
        assertNull(response.get("videoEvidenceUrl"));
        assertNull(response.get("picEvidenceUrl"));
        verifyNoInteractions(faceEvidenceService);
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

    @Test
    void providerFailureMessagesAreFilteredAndNeverLeakRawDetails() throws Exception {
        // 包含敏感或内部细节的错误消息应被过滤为脱敏通用安全提示
        String failWithRawLeak = "{\"success\":false,\"message\":\"internal id_no=110101199003072345 match fail against police db\"}";
        Map<String, Object> response = new Stub(failWithRawLeak).queryFaceAuthResult("server-token");
        assertEquals(false, response.get("success"));
        assertEquals("核身信息核验不通过，请重试", response.get("message"));
        assertFalse(response.toString().contains("110101199003072345"));
        assertFalse(response.toString().contains("police db"));

        // 过期场景安全过滤
        String expiredJson = "{\"success\":false,\"message\":\"verify token expired\"}";
        Map<String, Object> expiredResp = new Stub(expiredJson).queryFaceAuthResult("server-token");
        assertEquals(false, expiredResp.get("success"));
        assertEquals("核身流程已失效或过期，请重新发起", expiredResp.get("message"));
    }

    @Test
    void malformedJsonWithSentinelNeverLeaksRawContentOrCause() throws Exception {
        String badJson = "{\"success\":true,\"result\":{\"video\":\"" + "PRIVATE_VIDEO_SENTINEL_LEAK_DATA"
                + "\",\"verify_result\": MALFORMED_JSON_SYNTAX";

        Logger serviceLogger = (Logger) LoggerFactory.getLogger(BaiduFaceAuthServiceImpl.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        serviceLogger.addAppender(appender);

        try {
            Stub service = new Stub(badJson);
            Map<String, Object> response = service.queryFaceAuthResult("server-token");

            // 1. 返回必须是受控的安全错误结构
            assertEquals(false, response.get("success"));
            assertEquals("核身提供商响应解析失败", response.get("message"));
            assertNull(response.get("data"));

            // 2. 检查返回结构绝不包含哨兵值或原始片段
            String responseStr = response.toString();
            assertFalse(responseStr.contains("PRIVATE_VIDEO_SENTINEL"));
            assertFalse(responseStr.contains("MALFORMED_JSON_SYNTAX"));

            // 3. 验证日志捕获：不包含哨兵值、原始响应文本或异常原因/堆栈泄露
            boolean foundSentinelInLog = false;
            for (ILoggingEvent event : appender.list) {
                String formatted = event.getFormattedMessage();
                if (formatted != null && (formatted.contains("PRIVATE_VIDEO_SENTINEL") || formatted.contains("MALFORMED_JSON_SYNTAX"))) {
                    foundSentinelInLog = true;
                }
                if (event.getThrowableProxy() != null) {
                    String throwableMsg = event.getThrowableProxy().getMessage();
                    if (throwableMsg != null && (throwableMsg.contains("PRIVATE_VIDEO_SENTINEL") || throwableMsg.contains("MALFORMED_JSON_SYNTAX"))) {
                        foundSentinelInLog = true;
                    }
                }
            }
            assertFalse(foundSentinelInLog, "日志中不得泄漏 provider 原始报文或异常 cause 细节");
        } finally {
            serviceLogger.detachAppender(appender);
        }
    }

    @Test
    void getAccessTokenOrNetworkExceptionNeverLeaksDetailsAndReturnsSafeError() {
        Logger serviceLogger = (Logger) LoggerFactory.getLogger(BaiduFaceAuthServiceImpl.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        serviceLogger.addAppender(appender);

        try {
            BaiduFaceAuthServiceImpl brokenNetworkService = new BaiduFaceAuthServiceImpl() {
                @Override
                protected String getAccessToken() {
                    throw new RuntimeException("NETWORK_DOWN_SENSITIVE_API_KEY_LEAK");
                }
            };

            Map<String, Object> response = brokenNetworkService.queryFaceAuthResult("server-token");

            assertEquals(false, response.get("success"));
            assertEquals("核身提供商响应处理失败", response.get("message"));
            assertNull(response.get("data"));
            assertFalse(response.toString().contains("SENSITIVE_API_KEY_LEAK"));

            for (ILoggingEvent event : appender.list) {
                String formatted = event.getFormattedMessage();
                assertFalse(formatted.contains("SENSITIVE_API_KEY_LEAK"), "异常日志不得输出底层包含密钥或敏感细节的消息");
                assertNull(event.getThrowableProxy(), "脱敏异常边界不得原样输出 cause 异常堆栈");
            }
        } finally {
            serviceLogger.detachAppender(appender);
        }
    }

    @Test
    void malformedIdcardConfirmTypeEntersSafeFailureAndNeverLeaksSentinel() {
        String badIdcardJson = "{\"success\":true,\"result\":{\"verify_result\":{\"score\":98},"
                + "\"idcard_confirm\":\"SENTINEL_IDCARD_MALFORMED_TYPE\"}}";

        Logger serviceLogger = (Logger) LoggerFactory.getLogger(BaiduFaceAuthServiceImpl.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        serviceLogger.addAppender(appender);

        try {
            Stub service = new Stub(badIdcardJson);
            Map<String, Object> response = service.queryFaceAuthResult("server-token");

            // 1. 响应必须受控失败，data 清空
            assertEquals(false, response.get("success"));
            assertEquals("核身提供商响应处理失败", response.get("message"));
            assertNull(response.get("data"));
            assertFalse(response.toString().contains("SENTINEL_IDCARD"));

            // 2. 日志不得泄漏哨兵内容或原因
            boolean foundSentinel = false;
            for (ILoggingEvent event : appender.list) {
                String formatted = event.getFormattedMessage();
                if (formatted != null && formatted.contains("SENTINEL_IDCARD")) {
                    foundSentinel = true;
                }
                if (event.getThrowableProxy() != null) {
                    String msg = event.getThrowableProxy().getMessage();
                    if (msg != null && msg.contains("SENTINEL_IDCARD")) {
                        foundSentinel = true;
                    }
                }
            }
            assertFalse(foundSentinel, "日志中绝不得包含哨兵值或异常 cause");
        } finally {
            serviceLogger.detachAppender(appender);
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
