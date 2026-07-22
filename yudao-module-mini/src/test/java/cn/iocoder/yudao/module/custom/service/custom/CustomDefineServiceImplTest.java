package cn.iocoder.yudao.module.custom.service.custom;

import cn.iocoder.yudao.module.custom.dal.mysql.custom.CustomDefineMapper;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomDefineServiceImplTest {

    @Mock
    private CustomDefineMapper customDefineMapper;

    @Mock
    private ConfigApi configApi;

    @InjectMocks
    private CustomDefineServiceImpl service;

    @Test
    public void testSelectModel_exactMatch_ignoresConfig() {
        when(customDefineMapper.selectModel("1.2.3")).thenReturn("offcial");
        assertEquals("offcial", service.selectModel("1.2.3"));
    }

    @Test
    public void testSelectModel_noMatch_configOffcial_returnsOffcial() {
        when(customDefineMapper.selectModel("dev")).thenReturn(null);
        when(configApi.getConfigValueByKey(CustomDefineServiceImpl.CONFIG_KEY_DEFAULT_MODEL)).thenReturn("offcial");
        assertEquals("offcial", service.selectModel("dev"));
    }

    @Test
    public void testSelectModel_noMatch_configInvalidValue_fallsBackToSafe() {
        when(customDefineMapper.selectModel("dev")).thenReturn(null);
        when(configApi.getConfigValueByKey(CustomDefineServiceImpl.CONFIG_KEY_DEFAULT_MODEL)).thenReturn("xyz");
        assertEquals("safe", service.selectModel("dev"));
    }

    @Test
    public void testSelectModel_noMatch_configNull_fallsBackToSafe() {
        when(customDefineMapper.selectModel("dev")).thenReturn(null);
        when(configApi.getConfigValueByKey(CustomDefineServiceImpl.CONFIG_KEY_DEFAULT_MODEL)).thenReturn(null);
        assertEquals("safe", service.selectModel("dev"));
    }

    @Test
    public void testSelectModel_noMatch_configThrows_fallsBackToSafeWithoutCrash() {
        when(customDefineMapper.selectModel("dev")).thenReturn(null);
        when(configApi.getConfigValueByKey(CustomDefineServiceImpl.CONFIG_KEY_DEFAULT_MODEL))
                .thenThrow(new RuntimeException("config service down"));
        assertEquals("safe", service.selectModel("dev"));
    }

    @Test
    public void testSelectModel_noMatch_emptyStringVersion_fallsBackToConfig() {
        when(customDefineMapper.selectModel("dev")).thenReturn("");
        when(configApi.getConfigValueByKey(CustomDefineServiceImpl.CONFIG_KEY_DEFAULT_MODEL)).thenReturn("offcial");
        assertEquals("offcial", service.selectModel("dev"));
    }

}
