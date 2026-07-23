package cn.iocoder.yudao.module.custom.service.iconset;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.custom.controller.admin.iconset.vo.IconSetCloneReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.iconset.vo.IconSetProfileSaveReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.iconset.IconSetProfileDO;
import cn.iocoder.yudao.module.custom.dal.mysql.iconset.IconSetProfileMapper;
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
public class IconSetProfileServiceImplTest {

    @Mock
    private IconSetProfileMapper iconSetProfileMapper;

    @InjectMocks
    private IconSetProfileServiceImpl service;

    private IconSetProfileDO preset(Long id, boolean active) {
        IconSetProfileDO d = new IconSetProfileDO();
        d.setId(id);
        d.setName("预设图标集");
        d.setCode("preset-" + id);
        d.setType(IconSetProfileDO.TYPE_PRESET);
        Map<String, String> icons = new HashMap<>();
        icons.put("plus", "<svg>plus</svg>");
        d.setIcons(icons);
        d.setIsActive(active);
        d.setSort(1);
        return d;
    }

    private IconSetProfileDO custom(Long id, boolean active) {
        IconSetProfileDO d = preset(id, active);
        d.setType(IconSetProfileDO.TYPE_CUSTOM);
        d.setCode("custom-" + id);
        return d;
    }

    // ── useIconSetProfile ──────────────────────────────────────
    @Test
    public void testUseIconSetProfile_notExists_throws() {
        when(iconSetProfileMapper.selectById(1L)).thenReturn(null);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.useIconSetProfile(1L));
        assertEquals(10024, ex.getCode());
        verify(iconSetProfileMapper, never()).clearActive();
    }

    @Test
    public void testUseIconSetProfile_happy_clearsThenSetsActive() {
        when(iconSetProfileMapper.selectById(2L)).thenReturn(custom(2L, false));
        service.useIconSetProfile(2L);

        InOrder inOrder = inOrder(iconSetProfileMapper);
        inOrder.verify(iconSetProfileMapper).clearActive();
        inOrder.verify(iconSetProfileMapper).updateById(ArgumentMatchers.<IconSetProfileDO>argThat(d ->
                d.getId().equals(2L) && Boolean.TRUE.equals(d.getIsActive())));
    }

    // ── deleteIconSetProfile ─────────────────────────────────────
    @Test
    public void testDeleteIconSetProfile_presetCannotDelete_throws() {
        when(iconSetProfileMapper.selectById(1L)).thenReturn(preset(1L, false));
        ServiceException ex = assertThrows(ServiceException.class, () -> service.deleteIconSetProfile(1L));
        assertEquals(10026, ex.getCode());
        verify(iconSetProfileMapper, never()).deleteById(anyLong());
    }

    @Test
    public void testDeleteIconSetProfile_activeCannotDelete_throws() {
        when(iconSetProfileMapper.selectById(2L)).thenReturn(custom(2L, true));
        ServiceException ex = assertThrows(ServiceException.class, () -> service.deleteIconSetProfile(2L));
        assertEquals(10026, ex.getCode());
        verify(iconSetProfileMapper, never()).deleteById(anyLong());
    }

    @Test
    public void testDeleteIconSetProfile_happy_deletes() {
        when(iconSetProfileMapper.selectById(3L)).thenReturn(custom(3L, false));
        service.deleteIconSetProfile(3L);
        verify(iconSetProfileMapper).deleteById(3L);
    }

    // ── updateIconSetProfile ─────────────────────────────────────
    @Test
    public void testUpdateIconSetProfile_notExists_throws() {
        when(iconSetProfileMapper.selectById(1L)).thenReturn(null);
        IconSetProfileSaveReqVO reqVO = new IconSetProfileSaveReqVO();
        reqVO.setId(1L);
        reqVO.setName("x");
        ServiceException ex = assertThrows(ServiceException.class, () -> service.updateIconSetProfile(reqVO));
        assertEquals(10024, ex.getCode());
    }

    @Test
    public void testUpdateIconSetProfile_presetCoreFieldChanged_throws() {
        when(iconSetProfileMapper.selectById(1L)).thenReturn(preset(1L, false));
        IconSetProfileSaveReqVO reqVO = new IconSetProfileSaveReqVO();
        reqVO.setId(1L);
        reqVO.setName("改名");
        Map<String, String> icons = new HashMap<>();
        icons.put("plus", "<svg>changed</svg>");
        reqVO.setIcons(icons);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.updateIconSetProfile(reqVO));
        assertEquals(10025, ex.getCode());
        verify(iconSetProfileMapper, never()).updateById(any());
    }

    @Test
    public void testUpdateIconSetProfile_presetNonCoreFieldChanged_allowed() {
        when(iconSetProfileMapper.selectById(1L)).thenReturn(preset(1L, false));
        IconSetProfileSaveReqVO reqVO = new IconSetProfileSaveReqVO();
        reqVO.setId(1L);
        reqVO.setName("改名");
        reqVO.setSort(9);
        Map<String, String> icons = new HashMap<>();
        icons.put("plus", "<svg>plus</svg>"); // 与既有值相同，非核心字段变更
        reqVO.setIcons(icons);
        service.updateIconSetProfile(reqVO);
        verify(iconSetProfileMapper).updateById(any());
    }

    @Test
    public void testUpdateIconSetProfile_invalidSvg_throws() {
        when(iconSetProfileMapper.selectById(2L)).thenReturn(custom(2L, false));
        IconSetProfileSaveReqVO reqVO = new IconSetProfileSaveReqVO();
        reqVO.setId(2L);
        reqVO.setName("x");
        Map<String, String> icons = new HashMap<>();
        icons.put("plus", "<svg onload=alert(1)>bad</svg>");
        reqVO.setIcons(icons);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.updateIconSetProfile(reqVO));
        assertEquals(10027, ex.getCode());
    }

    // ── createIconSetProfile ─────────────────────────────────────
    @Test
    public void testCreateIconSetProfile_happy_insertsAsCustomInactive() {
        IconSetProfileSaveReqVO reqVO = new IconSetProfileSaveReqVO();
        reqVO.setName("我的自定义集");
        Map<String, String> icons = new HashMap<>();
        icons.put("plus", "<svg>plus</svg>");
        reqVO.setIcons(icons);

        service.createIconSetProfile(reqVO);

        verify(iconSetProfileMapper).insert(ArgumentMatchers.<IconSetProfileDO>argThat(d ->
                d.getType().equals(IconSetProfileDO.TYPE_CUSTOM) && Boolean.FALSE.equals(d.getIsActive())));
    }

    @Test
    public void testCreateIconSetProfile_invalidSvg_throws() {
        IconSetProfileSaveReqVO reqVO = new IconSetProfileSaveReqVO();
        reqVO.setName("x");
        Map<String, String> icons = new HashMap<>();
        icons.put("plus", "not-an-svg");
        reqVO.setIcons(icons);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.createIconSetProfile(reqVO));
        assertEquals(10027, ex.getCode());
        verify(iconSetProfileMapper, never()).insert(any());
    }

    // ── cloneAsCustom ─────────────────────────────────────────────
    @Test
    public void testCloneAsCustom_copiesIconsFromPreset() {
        IconSetProfileDO source = preset(1L, true);
        when(iconSetProfileMapper.selectById(1L)).thenReturn(source);
        IconSetCloneReqVO reqVO = new IconSetCloneReqVO();
        reqVO.setPresetId(1L);
        reqVO.setName("我的克隆集");

        service.cloneAsCustom(reqVO);

        verify(iconSetProfileMapper).insert(ArgumentMatchers.<IconSetProfileDO>argThat(d ->
                d.getType().equals(IconSetProfileDO.TYPE_CUSTOM)
                        && d.getSourcePresetId().equals(1L)
                        && d.getIcons().equals(source.getIcons())
                        && Boolean.FALSE.equals(d.getIsActive())));
    }

}
