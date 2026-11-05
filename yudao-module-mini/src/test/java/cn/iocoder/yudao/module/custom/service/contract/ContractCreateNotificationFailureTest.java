package cn.iocoder.yudao.module.custom.service.contract;

import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractSaveReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.wechat.vo.TemplateVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.custom.dal.mysql.contract.ContractMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.custom.CustomDefineMapper;
import cn.iocoder.yudao.module.custom.service.wechat.WechatService;
import cn.iocoder.yudao.module.custom.service.wechat.impl.WechatServiceImpl;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper;
import cn.iocoder.yudao.module.system.framework.idcard.IdCardCipherService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.WxMpTemplateMsgService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ContractCreateNotificationFailureTest {

    @AfterEach
    void clearLogin() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void missingCreatorRealnameAndFailedNotificationDoNotTurnInsertedContractIntoError() throws Exception {
        SecurityFrameworkUtils.setLoginUser(new LoginUser().setId(42L).setUserType(2), mock(HttpServletRequest.class));
        ContractServiceImpl service = new ContractServiceImpl();
        ContractAttachmentReferenceGuard attachments = mock(ContractAttachmentReferenceGuard.class);
        AdminUserMapper users = mock(AdminUserMapper.class);
        PasswordEncoder passwords = mock(PasswordEncoder.class);
        ContractMapper contracts = mock(ContractMapper.class);
        IdCardCipherService idCards = mock(IdCardCipherService.class);
        WechatService wechat = mock(WechatService.class);
        inject(service, "attachmentReferenceGuard", attachments);
        inject(service, "adminUserMapper", users);
        inject(service, "passwordEncoder", passwords);
        inject(service, "contractMapper", contracts);
        inject(service, "idCardCipherService", idCards);
        inject(service, "wechatService", wechat);

        AdminUserDO creator = new AdminUserDO();
        creator.setNickname("合同管理员");
        creator.setPayPassword("stored-hash");
        when(users.selectById(42L)).thenReturn(creator);
        when(passwords.matches("valid-payment-password", "stored-hash")).thenReturn(true);
        when(idCards.normalizeRequestToCipher(anyString())).thenAnswer(call -> call.getArgument(0));
        doAnswer(call -> {
            ContractDO row = call.getArgument(0);
            row.setId(777L);
            row.setCreateTime(LocalDateTime.of(2026, 11, 5, 18, 40));
            return 1;
        }).when(contracts).insert(any(ContractDO.class));
        when(wechat.send(any(TemplateVO.class))).thenThrow(new IllegalStateException("notification unavailable"));

        ContractSaveReqVO input = new ContractSaveReqVO();
        input.setIndebtedName("甲");
        input.setIndebtedId("borrower-id");
        input.setCreditorName("乙");
        input.setCreditorId("creditor-id");
        input.setSalary(BigDecimal.ONE);
        input.setPassword("valid-payment-password");

        assertEquals(777L, service.createContract(input));
        verify(contracts, times(1)).insert(any(ContractDO.class));
        org.mockito.ArgumentCaptor<TemplateVO> sent = org.mockito.ArgumentCaptor.forClass(TemplateVO.class);
        verify(wechat, times(2)).send(sent.capture());
        List<TemplateVO> notifications = sent.getAllValues();
        assertEquals("合同管理员", notifications.get(0).getDatas().get("phrase3"));
        assertEquals("creditor-id", notifications.get(0).getIdNo());
        assertEquals("borrower-id", notifications.get(1).getIdNo());
    }

    @Test
    void nullTemplateValueIsOmittedInsteadOfThrowingAfterContractInsert() throws Exception {
        WechatServiceImpl service = new WechatServiceImpl();
        AdminUserService users = mock(AdminUserService.class);
        CustomDefineMapper openIds = mock(CustomDefineMapper.class);
        WxMpService wx = mock(WxMpService.class);
        WxMpTemplateMsgService templates = mock(WxMpTemplateMsgService.class);
        inject(service, "adminUserService", users);
        inject(service, "customDefineMapper", openIds);
        inject(service, "wxMpService", wx);
        AdminUserDO recipient = new AdminUserDO();
        recipient.setId(7L);
        when(users.getUserByIdNo("recipient-id")).thenReturn(Collections.singletonList(recipient));
        when(openIds.selectOffcialOpenIdByUserId(7L)).thenReturn("openid");
        when(wx.getTemplateMsgService()).thenReturn(templates);
        when(templates.sendTemplateMsg(any(WxMpTemplateMessage.class))).thenReturn("message-id");

        TemplateVO input = new TemplateVO();
        input.setIdNo("recipient-id");
        input.setTemplateId("template-id");
        HashMap<String, Object> data = new HashMap<>();
        data.put("phrase3", null);
        data.put("amount6", 1);
        input.setDatas(data);

        assertEquals("message-id", service.send(input));
        org.mockito.ArgumentCaptor<WxMpTemplateMessage> sent = org.mockito.ArgumentCaptor.forClass(WxMpTemplateMessage.class);
        verify(templates).sendTemplateMsg(sent.capture());
        assertFalse(sent.getValue().getData().stream().anyMatch(item -> "phrase3".equals(item.getName())));
    }

    private static void inject(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
