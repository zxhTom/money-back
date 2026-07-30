package cn.iocoder.yudao.module.custom.service.miniconfig;

import cn.iocoder.yudao.module.custom.controller.admin.miniconfig.vo.MiniProgramConfigRespVO;
import cn.iocoder.yudao.module.custom.controller.admin.miniconfig.vo.MiniProgramConfigSaveReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.miniconfig.MiniProgramConfigDO;
import cn.iocoder.yudao.module.custom.dal.mysql.contract.ContractMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.miniconfig.MiniProgramConfigMapper;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MiniProgramConfigServiceImplTest {

    @Mock
    private MiniProgramConfigMapper miniProgramConfigMapper;
    @Mock
    private AdminUserMapper adminUserMapper;
    @Mock
    private ContractMapper contractMapper;

    @InjectMocks
    private MiniProgramConfigServiceImpl service;

    private MiniProgramConfigDO existing(String appName, Long boundUserId) {
        MiniProgramConfigDO c = new MiniProgramConfigDO();
        c.setId(1L);
        c.setAppName(appName);
        c.setSlogan("旧简介");
        c.setAppDescription("旧描述");
        c.setCompanyName("旧公司");
        c.setContactEmail("old@example.com");
        c.setBoundUserId(boundUserId);
        return c;
    }

    private MiniProgramConfigSaveReqVO reqVO(String appName, Long boundUserId) {
        MiniProgramConfigSaveReqVO vo = new MiniProgramConfigSaveReqVO();
        vo.setAppName(appName);
        vo.setSlogan("新简介");
        vo.setAppDescription("新描述");
        vo.setCompanyName("新公司");
        vo.setContactEmail("new@example.com");
        vo.setBoundUserId(boundUserId);
        return vo;
    }

    private AdminUserDO user(Long id, String idNo) {
        AdminUserDO u = new AdminUserDO();
        u.setId(id);
        u.setIdNo(idNo);
        return u;
    }

    @Test
    public void testUpdate_appNameUnchanged_noCascade() {
        when(miniProgramConfigMapper.selectTheOne()).thenReturn(existing("旧名称", 9L));

        service.update(reqVO("旧名称", 9L));

        verify(adminUserMapper, never()).updateById(any(AdminUserDO.class));
        verify(contractMapper, never()).updateIndebtedNameByIdCard(anyString(), anyString());
        verify(contractMapper, never()).updateCreditorNameByIdCard(anyString(), anyString());
    }

    @Test
    public void testUpdate_appNameChangedButNoBoundUser_noCascade() {
        when(miniProgramConfigMapper.selectTheOne()).thenReturn(existing("旧名称", null));

        service.update(reqVO("新名称", null));

        verify(adminUserMapper, never()).updateById(any(AdminUserDO.class));
        verify(contractMapper, never()).updateIndebtedNameByIdCard(anyString(), anyString());
    }

    @Test
    public void testUpdate_appNameChangedWithBoundUser_cascadesToUserAndContracts() {
        when(miniProgramConfigMapper.selectTheOne()).thenReturn(existing("旧名称", 9L));
        when(adminUserMapper.selectById(9L)).thenReturn(user(9L, "110101199001011234"));

        service.update(reqVO("新名称", 9L));

        ArgumentCaptor<AdminUserDO> captor = ArgumentCaptor.forClass(AdminUserDO.class);
        verify(adminUserMapper).updateById(captor.capture());
        assertEquals(9L, captor.getValue().getId());
        assertEquals("新名称", captor.getValue().getRealname());

        verify(contractMapper).updateIndebtedNameByIdCard("110101199001011234", "新名称");
        verify(contractMapper).updateCreditorNameByIdCard("110101199001011234", "新名称");
    }

    @Test
    public void testUpdate_rebindToNewUser_oldUserAndContractsUntouched() {
        // 从绑定用户9换绑到用户10，appName也变了：只应该对用户10联动，完全不碰用户9
        when(miniProgramConfigMapper.selectTheOne()).thenReturn(existing("旧名称", 9L));
        when(adminUserMapper.selectById(10L)).thenReturn(user(10L, "220202199002022345"));

        service.update(reqVO("新名称", 10L));

        verify(adminUserMapper, never()).selectById(9L);
        verify(contractMapper).updateIndebtedNameByIdCard("220202199002022345", "新名称");
        verify(contractMapper).updateCreditorNameByIdCard("220202199002022345", "新名称");
    }

    @Test
    public void testPreviewNameChangeImpact_noBoundUser_returnsZero() {
        when(miniProgramConfigMapper.selectTheOne()).thenReturn(existing("旧名称", null));

        assertEquals(0, service.previewNameChangeImpact());
        verify(contractMapper, never()).countByPartyIdCard(anyString());
    }

    @Test
    public void testPreviewNameChangeImpact_withBoundUser_returnsContractCount() {
        when(miniProgramConfigMapper.selectTheOne()).thenReturn(existing("旧名称", 9L));
        when(adminUserMapper.selectById(9L)).thenReturn(user(9L, "110101199001011234"));
        when(contractMapper.countByPartyIdCard("110101199001011234")).thenReturn(7);

        assertEquals(7, service.previewNameChangeImpact());
    }

    @Test
    public void testGetPublic_returnsOnlyDisplayFields() {
        when(miniProgramConfigMapper.selectTheOne()).thenReturn(existing("名称", 9L));

        MiniProgramConfigRespVO resp = service.getPublic();

        assertEquals("名称", resp.getAppName());
        assertEquals("旧简介", resp.getSlogan());
        assertEquals("旧描述", resp.getAppDescription());
        assertEquals("旧公司", resp.getCompanyName());
        assertEquals("old@example.com", resp.getContactEmail());
    }
}
