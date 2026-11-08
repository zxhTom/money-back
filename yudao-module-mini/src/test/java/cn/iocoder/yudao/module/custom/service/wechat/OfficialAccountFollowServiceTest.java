package cn.iocoder.yudao.module.custom.service.wechat;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.custom.config.WechatConfig;
import cn.iocoder.yudao.module.custom.dal.mysql.custom.CustomDefineMapper;
import cn.iocoder.yudao.module.custom.dto.Code2SessionResponse;
import cn.iocoder.yudao.module.custom.dto.MpVO;
import cn.iocoder.yudao.module.custom.service.custom.OfficialAccountFollowService;
import cn.iocoder.yudao.module.custom.service.wechat.WechatService;
import com.binarywang.spring.starter.wxjava.mp.properties.WxMpProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OfficialAccountFollowServiceTest {
    private final CustomDefineMapper mapper = mock(CustomDefineMapper.class);
    private final WechatService wechat = mock(WechatService.class);
    private OfficialAccountFollowService service;
    private WxMpProperties official;
    @BeforeEach void setup() {
        service = new OfficialAccountFollowService();
        WechatConfig mini = new WechatConfig(); mini.setAppid("mini-app");
        official = new WxMpProperties(); official.setAppId("official-app");
        ReflectionTestUtils.setField(service,"mapper",mapper);
        ReflectionTestUtils.setField(service,"wechatService",wechat);
        ReflectionTestUtils.setField(service,"miniConfig",mini);
        ReflectionTestUtils.setField(service,"officialConfig",official);
        TenantContextHolder.setTenantId(7L);
    }
    @AfterEach void cleanup() { TenantContextHolder.clear(); }
    private void currentWeChat() {
        Code2SessionResponse session = new Code2SessionResponse();
        session.setOpenid("current-openid"); session.setUnionid("verified-union");
        when(wechat.code2Session("fresh-code")).thenReturn(session);
    }
    @Test void currentWeChatCanBeFollowedWithoutBusinessAccountBinding() {
        currentWeChat();
        when(mapper.selectOfficialFollowStates(42L,7L,"mini-app","official-app","verified-union"))
                .thenReturn(Collections.singletonList(0));
        MpVO result = service.check(42L,"fresh-code");
        assertEquals(200,result.getCode()); assertEquals("FOLLOWED",result.getFollowState());
        verify(mapper).selectOfficialFollowStates(42L,7L,"mini-app","official-app","verified-union");
    }
    @Test void missingAssociationIsUnknownInsteadOfUnfollowed() {
        when(mapper.selectOfficialFollowStates(42L,7L,"mini-app","official-app",null)).thenReturn(Collections.emptyList());
        assertEquals("UNKNOWN",service.check(42L,null).getFollowState());
        assertEquals(1501,service.check(42L,null).getCode());
        verifyNoInteractions(wechat);
    }
    @Test void onlyExplicitUnfollowStatePrompts() {
        currentWeChat();
        when(mapper.selectOfficialFollowStates(42L,7L,"mini-app","official-app","verified-union"))
                .thenReturn(Collections.singletonList(1));
        MpVO result = service.check(42L,"fresh-code");
        assertEquals(1500,result.getCode()); assertEquals("UNFOLLOWED",result.getFollowState());
    }
    @Test void anyValidFollowedRecordWinsOverOldUnfollowRecord() {
        when(mapper.selectOfficialFollowStates(42L,7L,"mini-app","official-app",null)).thenReturn(Arrays.asList(1,0));
        assertEquals("FOLLOWED",service.check(42L,null).getFollowState());
    }
    @Test void missingUnionIdDoesNotFallBackToAnotherAccountsBinding() {
        Code2SessionResponse session = new Code2SessionResponse(); session.setOpenid("openid");
        when(wechat.code2Session("fresh-code")).thenReturn(session);
        assertEquals("UNKNOWN",service.check(42L,"fresh-code").getFollowState());
        verifyNoInteractions(mapper);
    }
    @Test void providerFailureDoesNotBecomeUnfollowed() {
        when(wechat.code2Session("fresh-code")).thenThrow(new RuntimeException("unavailable"));
        assertEquals("UNKNOWN",service.check(42L,"fresh-code").getFollowState());
        verifyNoInteractions(mapper);
    }
    @Test void clientCannotSubmitRawUnionIdOrChangeOfficialAppScope() {
        assertEquals("UNKNOWN",service.check(42L,"union&appid=other").getFollowState());
        verifyNoInteractions(wechat,mapper);
        official.setAppId("");
        assertEquals("UNKNOWN",service.check(42L,"fresh-code").getFollowState());
        verifyNoInteractions(wechat,mapper);
    }
    @Test void missingLoginNeverQueriesOtherUsers() {
        assertEquals("UNKNOWN",service.check(null,null).getFollowState());
        verifyNoInteractions(mapper,wechat);
    }
    @Test void backgroundFollowCheckDoesNotTriggerFaceOrPaymentPasswordDialogs() throws Exception {
        Class<?>[] guards = {
            cn.iocoder.yudao.module.custom.framework.security.interceptor.RealNameAuthInterceptor.class,
            cn.iocoder.yudao.module.custom.framework.security.interceptor.PayPasswordInterceptor.class
        };
        for (Class<?> guard : guards) {
            java.lang.reflect.Method allowed = guard.getDeclaredMethod("isAllowed",Long.class,String.class);
            allowed.setAccessible(true);
            assertEquals(Boolean.TRUE,allowed.invoke(guard.getDeclaredConstructor().newInstance(),42L,
                    "/custom/contract/dashboard/contactMp/check"));
        }
    }
}
