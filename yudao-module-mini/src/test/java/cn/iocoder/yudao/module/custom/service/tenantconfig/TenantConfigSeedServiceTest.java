package cn.iocoder.yudao.module.custom.service.tenantconfig;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.tenant.config.TenantProperties;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.custom.dal.dataobject.changelog.VersionChangelogDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractModelDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.iconset.IconSetProfileDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.miniconfig.MiniProgramConfigDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.skin.SkinProfileDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.speedcontrol.SpeedControlConfigDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.text.TextItemDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.text.TextProfileDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.timewindow.TimeWindowDO;
import cn.iocoder.yudao.module.custom.dal.mysql.changelog.VersionChangelogMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.contract.ContractModelMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.iconset.IconSetProfileMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.miniconfig.MiniProgramConfigMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.skin.SkinProfileMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.speedcontrol.SpeedControlConfigMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.text.TextItemMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.text.TextProfileMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.timewindow.TimeWindowMapper;
import cn.iocoder.yudao.module.fee.dal.dataobject.strategy.StrategyDO;
import cn.iocoder.yudao.module.fee.dal.mysql.strategy.StrategyMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TenantConfigSeedServiceTest {

    @Mock
    private TenantProperties tenantProperties;

    @Mock
    private SkinProfileMapper skinProfileMapper;
    @Mock
    private TextProfileMapper textProfileMapper;
    @Mock
    private TextItemMapper textItemMapper;
    @Mock
    private IconSetProfileMapper iconSetProfileMapper;
    @Mock
    private MiniProgramConfigMapper miniProgramConfigMapper;
    @Mock
    private VersionChangelogMapper versionChangelogMapper;
    @Mock
    private SpeedControlConfigMapper speedControlConfigMapper;
    @Mock
    private StrategyMapper strategyMapper;
    @Mock
    private ContractModelMapper contractModelMapper;
    @Mock
    private TimeWindowMapper timeWindowMapper;

    @InjectMocks
    private TenantConfigSeedServiceImpl seedService;

    @BeforeEach
    public void setUp() {
        TenantContextHolder.clear();
    }

    @AfterEach
    public void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    public void test1_TargetTenantAllEmpty_CopiesSuccessfully() {
        when(tenantProperties.getDefaultTenantId()).thenReturn(1L);

        SkinProfileDO skinDO = new SkinProfileDO();
        skinDO.setId(10L);
        skinDO.setCode("default_skin");
        when(skinProfileMapper.selectList()).thenReturn(Collections.singletonList(skinDO));

        TextProfileDO textProfileDO = new TextProfileDO();
        textProfileDO.setId(20L);
        textProfileDO.setCode("default_text");
        when(textProfileMapper.selectList()).thenReturn(Collections.singletonList(textProfileDO));

        TextItemDO textItemDO = new TextItemDO();
        textItemDO.setId(30L);
        textItemDO.setProfileId(20L);
        textItemDO.setItemKey("key1");
        when(textItemMapper.selectList()).thenReturn(Collections.singletonList(textItemDO));

        Map<String, Integer> result = seedService.seedTenantConfig(162L);

        assertNotNull(result);
        assertEquals(1, result.get("custom_skin_profile"));
        assertEquals(1, result.get("custom_text_profile"));
        assertEquals(1, result.get("custom_text_item"));

        verify(skinProfileMapper, times(1)).insert(any(SkinProfileDO.class));
        verify(textProfileMapper, times(1)).insert(any(TextProfileDO.class));
        verify(textItemMapper, times(1)).insert(any(TextItemDO.class));
    }

    @Test
    public void test2_TargetTenantSkinHasData_SkinSkipped() {
        when(tenantProperties.getDefaultTenantId()).thenReturn(1L);

        // Target tenant 162 has skin data
        when(skinProfileMapper.selectCount()).thenReturn(1L);

        Map<String, Integer> result = seedService.seedTenantConfig(162L);

        assertNotNull(result);
        assertEquals(-1, result.get("custom_skin_profile"));
        verify(skinProfileMapper, never()).insert(any(SkinProfileDO.class));
    }

    @Test
    public void test3_TextItemCopy_UsesNewProfileId() {
        when(tenantProperties.getDefaultTenantId()).thenReturn(1L);

        TextProfileDO oldProfile = new TextProfileDO();
        oldProfile.setId(100L);
        oldProfile.setCode("p1");
        when(textProfileMapper.selectList()).thenReturn(Collections.singletonList(oldProfile));

        // Simulate MyBatis-Plus assigning new ID on insert
        doAnswer(invocation -> {
            TextProfileDO p = invocation.getArgument(0);
            p.setId(200L);
            return 1;
        }).when(textProfileMapper).insert(any(TextProfileDO.class));

        TextItemDO oldItem = new TextItemDO();
        oldItem.setId(1000L);
        oldItem.setProfileId(100L);
        oldItem.setItemKey("k1");
        when(textItemMapper.selectList()).thenReturn(Collections.singletonList(oldItem));

        Map<String, Integer> result = seedService.seedTenantConfig(162L);

        assertEquals(1, result.get("custom_text_profile"));
        assertEquals(1, result.get("custom_text_item"));

        org.mockito.ArgumentCaptor<TextItemDO> captor = org.mockito.ArgumentCaptor.forClass(TextItemDO.class);
        verify(textItemMapper).insert(captor.capture());
        assertEquals(200L, captor.getValue().getProfileId());
    }

    @Test
    public void test4_NullTenantProperties_ThrowsException() {
        ReflectionTestUtils.setField(seedService, "tenantProperties", null);

        ServiceException exception = assertThrows(ServiceException.class, () -> seedService.seedTenantConfig(162L));
        assertTrue(exception.getMessage().contains("请先开启多租户后再初始化配置"));
        verifyNoInteractions(skinProfileMapper, textProfileMapper, textItemMapper);
    }

    @Test
    public void test5_TargetTenantEqualsSourceTenant_ThrowsException() {
        when(tenantProperties.getDefaultTenantId()).thenReturn(1L);

        ServiceException exception = assertThrows(ServiceException.class, () -> seedService.seedTenantConfig(1L));
        assertTrue(exception.getMessage().contains("目标租户不能与源租户相同"));
        verifyNoInteractions(skinProfileMapper, textProfileMapper, textItemMapper);
    }

    @Test
    public void test6_ResetFields_IdAndTenantIdNull() {
        when(tenantProperties.getDefaultTenantId()).thenReturn(1L);

        SkinProfileDO sourceSkin = new SkinProfileDO();
        sourceSkin.setId(999L);
        sourceSkin.setTenantId(1L);
        sourceSkin.setCode("skin_code");
        when(skinProfileMapper.selectList()).thenReturn(Collections.singletonList(sourceSkin));

        seedService.seedTenantConfig(162L);

        org.mockito.ArgumentCaptor<SkinProfileDO> captor = org.mockito.ArgumentCaptor.forClass(SkinProfileDO.class);
        verify(skinProfileMapper).insert(captor.capture());
        SkinProfileDO insertedSkin = captor.getValue();
        assertNull(insertedSkin.getId(), "插入前 id 必须被重置为 null");
        assertNull(insertedSkin.getTenantId(), "插入前 tenantId 必须被重置为 null");
    }
}
