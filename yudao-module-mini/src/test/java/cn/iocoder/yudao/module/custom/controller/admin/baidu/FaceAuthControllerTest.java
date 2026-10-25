package cn.iocoder.yudao.module.custom.controller.admin.baidu;

import cn.iocoder.yudao.module.custom.controller.admin.baidu.vo.BaiduUserInfo;
import cn.iocoder.yudao.module.custom.framework.faceauth.FaceAuthCallbackTokenStore;
import cn.iocoder.yudao.module.custom.service.face.baidu.BaiduFaceAuthService;
import cn.iocoder.yudao.module.custom.service.wechat.WechatService;
import cn.iocoder.yudao.module.system.framework.idcard.IdCardCipherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FaceAuthControllerTest {

    @Mock
    private BaiduFaceAuthService baiduFaceAuthService;
    @Mock
    private WechatService wechatService;
    @Mock
    private IdCardCipherService idCardCipherService;
    @Mock
    private FaceAuthCallbackTokenStore faceAuthCallbackTokenStore;
    @Mock
    private cn.iocoder.yudao.module.custom.service.face.FaceAuthSessionGuard sessionGuard;
    @Mock
    private cn.iocoder.yudao.module.custom.service.face.evidence.FaceEvidenceService faceEvidenceService;

    @InjectMocks
    private FaceAuthController controller;

    @BeforeEach
    public void setUp() {
        lenient().when(idCardCipherService.decryptForExternalApi(anyString())).thenReturn("idCardPlain");
        cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO caller = new cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO();
        caller.setId(1024L);
        caller.setRealname("张三");
        caller.setIdNo("idCardPlain");
        lenient().when(sessionGuard.requireIdentity(anyString())).thenReturn(caller);
    }

    @Test
    public void testStartFaceAuth_storesIdCardToVerifyTokenMapping() throws Exception {
        when(baiduFaceAuthService.getVerifyToken(anyString(), anyString())).thenReturn("verifyTokenAbc");

        controller.startFaceAuth("idCardPlain");

        verify(faceAuthCallbackTokenStore).store("idCardPlain", "verifyTokenAbc");
    }

    @Test
    public void testReportUserInfo_neverMarksVerified() throws Exception {
        // 根因回归测试：这一步（提交姓名+身份证）绝不能让 verified 被置 1，
        // 无论 baiduFaceAuthService.reportUserInfo 返回 true 还是 false。
        BaiduUserInfo info = new BaiduUserInfo();
        info.setVerifyToken("verifyTokenAbc");
        info.setName("张三");
        info.setIdCard("idCardCipherOrPlain");
        when(baiduFaceAuthService.reportUserInfo(anyString(), anyString(), anyString())).thenReturn(true);

        controller.reportUserInfo(info);

        verify(wechatService, never()).updateVerify(anyString(), anyInt());
    }

    @Test
    public void testEvidenceDownload_headersAndOutput() throws Exception {
        cn.iocoder.yudao.module.custom.dal.dataobject.face.FaceEvidenceDO evidence = new cn.iocoder.yudao.module.custom.dal.dataobject.face.FaceEvidenceDO();
        evidence.setId(88L);
        evidence.setMediaType("best_image");
        evidence.setContent(new byte[]{1, 2, 3});
        when(faceEvidenceService.getAuthorizedEvidence(88L)).thenReturn(evidence);

        javax.servlet.http.HttpServletResponse response = mock(javax.servlet.http.HttpServletResponse.class);
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        javax.servlet.ServletOutputStream sos = new javax.servlet.ServletOutputStream() {
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(javax.servlet.WriteListener listener) {}
            @Override public void write(int b) { out.write(b); }
        };
        when(response.getOutputStream()).thenReturn(sos);

        controller.getFaceEvidence(88L, response);

        verify(response).setHeader(eq("Cache-Control"), contains("no-store"));
        verify(response).setHeader(eq("X-Content-Type-Options"), eq("nosniff"));
        verify(response).setContentType("image/jpeg");
        assertArrayEquals(new byte[]{1, 2, 3}, out.toByteArray());
    }
}
