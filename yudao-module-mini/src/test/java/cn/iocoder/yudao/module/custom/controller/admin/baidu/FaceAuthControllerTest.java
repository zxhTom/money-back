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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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

    @InjectMocks
    private FaceAuthController controller;

    @BeforeEach
    public void setUp() {
        lenient().when(idCardCipherService.decryptForExternalApi(anyString())).thenReturn("idCardPlain");
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
}
