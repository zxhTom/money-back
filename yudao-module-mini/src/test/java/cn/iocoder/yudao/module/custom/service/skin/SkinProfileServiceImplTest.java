package cn.iocoder.yudao.module.custom.service.skin;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.custom.controller.admin.skin.vo.SkinCloneReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.skin.vo.SkinProfileSaveReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.skin.SkinProfileDO;
import cn.iocoder.yudao.module.custom.dal.mysql.skin.SkinProfileMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SkinProfileServiceImplTest {

    @Mock
    private SkinProfileMapper skinProfileMapper;

    @InjectMocks
    private SkinProfileServiceImpl service;

    private SkinProfileDO preset(Long id, boolean active) {
        SkinProfileDO d = new SkinProfileDO();
        d.setId(id);
        d.setName("预设皮肤");
        d.setCode("preset-" + id);
        d.setType(SkinProfileDO.TYPE_PRESET);
        d.setConfigMode(SkinProfileDO.CONFIG_MODE_BASIC);
        Map<String, String> tokens = new HashMap<>();
        tokens.put("--color-primary", "#6C4FF2");
        d.setTokens(tokens);
        d.setCustomCssText(null);
        d.setIsActive(active);
        d.setSort(1);
        return d;
    }

    private SkinProfileDO custom(Long id, boolean active) {
        SkinProfileDO d = preset(id, active);
        d.setType(SkinProfileDO.TYPE_CUSTOM);
        d.setCode("custom-" + id);
        return d;
    }

    // ── useSkinProfile ──────────────────────────────────────────
    @Test
    public void testUseSkinProfile_notExists_throws() {
        when(skinProfileMapper.selectById(1L)).thenReturn(null);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.useSkinProfile(1L));
        assertEquals(10018, ex.getCode());
        verify(skinProfileMapper, never()).clearActive();
    }

    @Test
    public void testUseSkinProfile_happy_clearsThenSetsActive() {
        when(skinProfileMapper.selectById(2L)).thenReturn(custom(2L, false));
        service.useSkinProfile(2L);

        InOrder inOrder = inOrder(skinProfileMapper);
        inOrder.verify(skinProfileMapper).clearActive();
        inOrder.verify(skinProfileMapper).updateById(ArgumentMatchers.<SkinProfileDO>argThat(d ->
                d.getId().equals(2L) && Boolean.TRUE.equals(d.getIsActive())));
    }

    // ── deleteSkinProfile ───────────────────────────────────────
    @Test
    public void testDeleteSkinProfile_presetCannotDelete_throws() {
        when(skinProfileMapper.selectById(1L)).thenReturn(preset(1L, false));
        ServiceException ex = assertThrows(ServiceException.class, () -> service.deleteSkinProfile(1L));
        assertEquals(10020, ex.getCode());
        verify(skinProfileMapper, never()).deleteById(anyLong());
    }

    @Test
    public void testDeleteSkinProfile_activeCannotDelete_throws() {
        when(skinProfileMapper.selectById(2L)).thenReturn(custom(2L, true));
        ServiceException ex = assertThrows(ServiceException.class, () -> service.deleteSkinProfile(2L));
        assertEquals(10020, ex.getCode());
        verify(skinProfileMapper, never()).deleteById(anyLong());
    }

    @Test
    public void testDeleteSkinProfile_happy_deletes() {
        when(skinProfileMapper.selectById(3L)).thenReturn(custom(3L, false));
        service.deleteSkinProfile(3L);
        verify(skinProfileMapper).deleteById(3L);
    }

    // ── updateSkinProfile ───────────────────────────────────────
    @Test
    public void testUpdateSkinProfile_notExists_throws() {
        when(skinProfileMapper.selectById(1L)).thenReturn(null);
        SkinProfileSaveReqVO reqVO = new SkinProfileSaveReqVO();
        reqVO.setId(1L);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.updateSkinProfile(reqVO));
        assertEquals(10018, ex.getCode());
    }

    @Test
    public void testUpdateSkinProfile_presetCoreFieldChanged_throws() {
        when(skinProfileMapper.selectById(1L)).thenReturn(preset(1L, false));
        SkinProfileSaveReqVO reqVO = new SkinProfileSaveReqVO();
        reqVO.setId(1L);
        reqVO.setName("改名");
        reqVO.setConfigMode(SkinProfileDO.CONFIG_MODE_BASIC);
        Map<String, String> tokens = new HashMap<>();
        tokens.put("--color-primary", "#000000"); // 改动了 token 值
        reqVO.setTokens(tokens);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.updateSkinProfile(reqVO));
        assertEquals(10019, ex.getCode());
        verify(skinProfileMapper, never()).updateById(ArgumentMatchers.<SkinProfileDO>any());
    }

    @Test
    public void testUpdateSkinProfile_presetOnlyNameChanged_succeeds() {
        SkinProfileDO existing = preset(1L, false);
        when(skinProfileMapper.selectById(1L)).thenReturn(existing);
        SkinProfileSaveReqVO reqVO = new SkinProfileSaveReqVO();
        reqVO.setId(1L);
        reqVO.setName("新名字");
        reqVO.setSort(9);
        reqVO.setThumbnailUrl("https://example.com/new-thumbnail.png");
        reqVO.setConfigMode(existing.getConfigMode());
        reqVO.setTokens(new HashMap<>(existing.getTokens())); // 未变化
        reqVO.setCustomCssText(existing.getCustomCssText());

        service.updateSkinProfile(reqVO);

        verify(skinProfileMapper).updateById(ArgumentMatchers.<SkinProfileDO>argThat(d ->
                d.getId().equals(1L) && "新名字".equals(d.getName())
                        && d.getSort().equals(9)
                        && "https://example.com/new-thumbnail.png".equals(d.getThumbnailUrl())
                        && d.getTokens() == null // 预设不重写 tokens 字段
                        && d.getConfigMode() == null));
    }

    @Test
    public void testUpdateSkinProfile_custom_updatesCoreFields() {
        when(skinProfileMapper.selectById(2L)).thenReturn(custom(2L, false));
        SkinProfileSaveReqVO reqVO = new SkinProfileSaveReqVO();
        reqVO.setId(2L);
        reqVO.setName("自定义皮肤");
        reqVO.setConfigMode(SkinProfileDO.CONFIG_MODE_ADVANCED);
        Map<String, String> tokens = new HashMap<>();
        tokens.put("--color-primary", "#111111");
        reqVO.setTokens(tokens);
        reqVO.setCustomCssText("--radius-lg: 40rpx;");

        service.updateSkinProfile(reqVO);

        verify(skinProfileMapper).updateById(ArgumentMatchers.<SkinProfileDO>argThat(d ->
                d.getConfigMode().equals(SkinProfileDO.CONFIG_MODE_ADVANCED)
                        && d.getTokens().equals(tokens)
                        && "--radius-lg: 40rpx;".equals(d.getCustomCssText())));
    }

    @Test
    public void testUpdateSkinProfile_invalidCssText_throws() {
        when(skinProfileMapper.selectById(2L)).thenReturn(custom(2L, false));
        SkinProfileSaveReqVO reqVO = new SkinProfileSaveReqVO();
        reqVO.setId(2L);
        reqVO.setName("x");
        reqVO.setConfigMode(SkinProfileDO.CONFIG_MODE_ADVANCED);
        reqVO.setTokens(new HashMap<>());
        reqVO.setCustomCssText(".evil { color: red; }");

        ServiceException ex = assertThrows(ServiceException.class, () -> service.updateSkinProfile(reqVO));
        assertEquals(10021, ex.getCode());
    }

    // ── createSkinProfile ───────────────────────────────────────
    @Test
    public void testCreateSkinProfile_setsTypeCustomAndInactive() {
        SkinProfileSaveReqVO reqVO = new SkinProfileSaveReqVO();
        reqVO.setName("新皮肤");
        reqVO.setConfigMode(SkinProfileDO.CONFIG_MODE_BASIC);
        reqVO.setTokens(new HashMap<>());

        service.createSkinProfile(reqVO);

        verify(skinProfileMapper).insert(ArgumentMatchers.<SkinProfileDO>argThat(d ->
                d.getType().equals(SkinProfileDO.TYPE_CUSTOM) && Boolean.FALSE.equals(d.getIsActive())));
    }

    @Test
    public void testCreateSkinProfile_invalidCssText_throws() {
        SkinProfileSaveReqVO reqVO = new SkinProfileSaveReqVO();
        reqVO.setName("新皮肤");
        reqVO.setConfigMode(SkinProfileDO.CONFIG_MODE_ADVANCED);
        reqVO.setTokens(new HashMap<>());
        reqVO.setCustomCssText("@media (min-width: 1px) { --a: 1; }");

        ServiceException ex = assertThrows(ServiceException.class, () -> service.createSkinProfile(reqVO));
        assertEquals(10021, ex.getCode());
        verify(skinProfileMapper, never()).insert(ArgumentMatchers.<SkinProfileDO>any());
    }

    // ── cloneAsCustom ───────────────────────────────────────────
    @Test
    public void testCloneAsCustom_sourceNotExists_throws() {
        when(skinProfileMapper.selectById(1L)).thenReturn(null);
        SkinCloneReqVO reqVO = new SkinCloneReqVO();
        reqVO.setPresetId(1L);
        reqVO.setName("克隆皮肤");
        ServiceException ex = assertThrows(ServiceException.class, () -> service.cloneAsCustom(reqVO));
        assertEquals(10018, ex.getCode());
    }

    @Test
    public void testCloneAsCustom_deepCopyCorrectness() {
        SkinProfileDO source = preset(1L, true);
        source.setConfigMode(SkinProfileDO.CONFIG_MODE_ADVANCED);
        source.setCustomCssText("--radius-lg: 40rpx;");
        when(skinProfileMapper.selectById(1L)).thenReturn(source);

        SkinCloneReqVO reqVO = new SkinCloneReqVO();
        reqVO.setPresetId(1L);
        reqVO.setName("我的皮肤");

        service.cloneAsCustom(reqVO);

        verify(skinProfileMapper).insert(ArgumentMatchers.<SkinProfileDO>argThat(d -> {
            boolean basic = d.getType().equals(SkinProfileDO.TYPE_CUSTOM)
                    && d.getSourcePresetId().equals(1L)
                    && d.getCode().startsWith("custom-")
                    && d.getName().equals("我的皮肤")
                    && d.getConfigMode().equals(SkinProfileDO.CONFIG_MODE_ADVANCED)
                    && d.getCustomCssText().equals("--radius-lg: 40rpx;")
                    && Boolean.FALSE.equals(d.getIsActive())
                    && d.getTokens().equals(source.getTokens())
                    && d.getTokens() != source.getTokens(); // 深拷贝，非同一实例
            return basic;
        }));

        // 修改源 tokens 不应影响已克隆的 map（因为 insert 时已是独立副本）
        source.getTokens().put("--color-primary", "#FFFFFF");
    }

    // ── getActiveSkinProfile ────────────────────────────────────
    @Test
    public void testGetActiveSkinProfile_delegatesToMapper() {
        SkinProfileDO active = custom(1L, true);
        when(skinProfileMapper.selectActive()).thenReturn(active);
        assertSame(active, service.getActiveSkinProfile());
    }

    @Test
    public void testGetActiveSkinProfile_notFound_returnsNull() {
        when(skinProfileMapper.selectActive()).thenReturn(null);
        assertNull(service.getActiveSkinProfile());
    }

}
