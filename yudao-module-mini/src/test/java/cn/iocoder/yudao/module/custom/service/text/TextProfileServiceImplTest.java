package cn.iocoder.yudao.module.custom.service.text;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.custom.controller.admin.text.vo.TextProfileSaveReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.text.TextItemDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.text.TextProfileDO;
import cn.iocoder.yudao.module.custom.dal.mysql.text.TextItemMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.text.TextProfileMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TextProfileServiceImplTest {

    @Mock
    private TextProfileMapper textProfileMapper;

    @Mock
    private TextItemMapper textItemMapper;

    @InjectMocks
    private TextProfileServiceImpl service;

    private TextProfileDO profile(Long id, boolean active) {
        return profile(id, active, "safe");
    }

    private TextProfileDO profile(Long id, boolean active, String textMode) {
        TextProfileDO d = new TextProfileDO();
        d.setId(id);
        d.setName("默认文案");
        d.setCode("text-" + id);
        d.setSeedFrom("safe");
        d.setTextMode(textMode);
        d.setIsActive(active);
        d.setSort(1);
        return d;
    }

    private TextItemDO item(Long id, Long profileId, String key, String value) {
        TextItemDO i = new TextItemDO();
        i.setId(id);
        i.setProfileId(profileId);
        i.setPageKey("contract.contractDetail");
        i.setItemKey(key);
        i.setItemValue(value);
        i.setSort(1);
        return i;
    }

    // ── useTextProfile ──────────────────────────────────────────
    @Test
    public void testUseTextProfile_notExists_throws() {
        when(textProfileMapper.selectById(1L)).thenReturn(null);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.useTextProfile(1L));
        assertEquals(10022, ex.getCode());
        verify(textProfileMapper, never()).clearActive(anyString());
    }

    @Test
    public void testUseTextProfile_happy_clearsThenSetsActive() {
        when(textProfileMapper.selectById(2L)).thenReturn(profile(2L, false, "safe"));
        service.useTextProfile(2L);

        InOrder inOrder = inOrder(textProfileMapper);
        inOrder.verify(textProfileMapper).clearActive("safe");
        inOrder.verify(textProfileMapper).updateById(ArgumentMatchers.<TextProfileDO>argThat(d ->
                d.getId().equals(2L) && Boolean.TRUE.equals(d.getIsActive())));
    }

    @Test
    public void testUseTextProfile_offcialTarget_clearsOnlyOffcialMode() {
        when(textProfileMapper.selectById(3L)).thenReturn(profile(3L, false, "offcial"));
        service.useTextProfile(3L);

        verify(textProfileMapper).clearActive("offcial");
        verify(textProfileMapper, never()).clearActive("safe");
    }

    /**
     * 核心行为变化：激活一个 textMode 的套时，只应清空同 textMode 内的生效标记，
     * 不应波及另一个 textMode——从而 safe 套和 offcial 套可以同时各有一条生效。
     */
    @Test
    public void testUseTextProfile_safeAndOffcialActivationsAreIndependent() {
        // 场景一：激活一个 safe 套（此时已有一个 offcial 套生效中）
        when(textProfileMapper.selectById(10L)).thenReturn(profile(10L, false, "safe"));
        service.useTextProfile(10L);
        verify(textProfileMapper).clearActive("safe");
        verify(textProfileMapper, never()).clearActive("offcial");

        reset(textProfileMapper);

        // 场景二：激活一个 offcial 套（此时已有一个 safe 套生效中）
        when(textProfileMapper.selectById(20L)).thenReturn(profile(20L, false, "offcial"));
        service.useTextProfile(20L);
        verify(textProfileMapper).clearActive("offcial");
        verify(textProfileMapper, never()).clearActive("safe");
    }

    // ── deleteTextProfile ───────────────────────────────────────
    @Test
    public void testDeleteTextProfile_notExists_throws() {
        when(textProfileMapper.selectById(1L)).thenReturn(null);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.deleteTextProfile(1L));
        assertEquals(10022, ex.getCode());
        verify(textProfileMapper, never()).deleteById(anyLong());
    }

    @Test
    public void testDeleteTextProfile_activeCannotDelete_throws() {
        when(textProfileMapper.selectById(2L)).thenReturn(profile(2L, true));
        ServiceException ex = assertThrows(ServiceException.class, () -> service.deleteTextProfile(2L));
        assertEquals(10023, ex.getCode());
        verify(textProfileMapper, never()).deleteById(anyLong());
        verify(textItemMapper, never()).deleteByProfileId(anyLong());
    }

    @Test
    public void testDeleteTextProfile_happy_deletesAndCascadesItems() {
        when(textProfileMapper.selectById(3L)).thenReturn(profile(3L, false));
        service.deleteTextProfile(3L);
        verify(textProfileMapper).deleteById(3L);
        verify(textItemMapper).deleteByProfileId(3L);

        // 先删子表 items 再删父表 profile，即使无事务保护也不会留下孤儿数据
        InOrder inOrder = inOrder(textItemMapper, textProfileMapper);
        inOrder.verify(textItemMapper).deleteByProfileId(3L);
        inOrder.verify(textProfileMapper).deleteById(3L);
    }

    // ── updateTextProfile ───────────────────────────────────────
    @Test
    public void testUpdateTextProfile_notExists_throws() {
        when(textProfileMapper.selectById(1L)).thenReturn(null);
        TextProfileSaveReqVO reqVO = new TextProfileSaveReqVO();
        reqVO.setId(1L);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.updateTextProfile(reqVO));
        assertEquals(10022, ex.getCode());
    }

    @Test
    public void testUpdateTextProfile_happy_onlyUpdatesNameAndRemark() {
        when(textProfileMapper.selectById(1L)).thenReturn(profile(1L, false, "safe"));
        TextProfileSaveReqVO reqVO = new TextProfileSaveReqVO();
        reqVO.setId(1L);
        reqVO.setName("新名字");
        reqVO.setRemark("备注");
        reqVO.setTextMode("offcial"); // 即使请求体带了不同的 textMode，也必须被忽略——创建后不可变

        service.updateTextProfile(reqVO);

        verify(textProfileMapper).updateById(ArgumentMatchers.<TextProfileDO>argThat(d ->
                d.getId().equals(1L) && "新名字".equals(d.getName()) && "备注".equals(d.getRemark())
                        && d.getCode() == null && d.getIsActive() == null && d.getSeedFrom() == null
                        && d.getTextMode() == null));
    }

    // ── createTextProfile ───────────────────────────────────────
    @Test
    public void testCreateTextProfile_setsCodeSeedFromAndInactive() {
        TextProfileSaveReqVO reqVO = new TextProfileSaveReqVO();
        reqVO.setName("新文案套");
        reqVO.setTextMode("safe");

        service.createTextProfile(reqVO);

        verify(textProfileMapper).insert(ArgumentMatchers.<TextProfileDO>argThat(d ->
                "新文案套".equals(d.getName())
                        && d.getCode() != null && d.getCode().startsWith("text-")
                        && "safe".equals(d.getSeedFrom())
                        && "safe".equals(d.getTextMode())
                        && Boolean.FALSE.equals(d.getIsActive())));
    }

    @Test
    public void testCreateTextProfile_offcialTextMode_seedFromStillSafe() {
        // seedFrom 与 textMode 是两个独立字段：即使新建的是 offcial 类型套，seedFrom 依然固定为 "safe"（既有语义不变）
        TextProfileSaveReqVO reqVO = new TextProfileSaveReqVO();
        reqVO.setName("官方新文案套");
        reqVO.setTextMode("offcial");

        service.createTextProfile(reqVO);

        verify(textProfileMapper).insert(ArgumentMatchers.<TextProfileDO>argThat(d ->
                "offcial".equals(d.getTextMode())
                        && "safe".equals(d.getSeedFrom())));
    }

    // ── cloneProfile ────────────────────────────────────────────
    @Test
    public void testCloneProfile_sourceNotExists_throws() {
        when(textProfileMapper.selectById(1L)).thenReturn(null);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.cloneProfile(1L, "克隆文案"));
        assertEquals(10022, ex.getCode());
    }

    @Test
    public void testCloneProfile_copiesAllItemsToNewProfile() {
        TextProfileDO source = profile(1L, true, "offcial");
        when(textProfileMapper.selectById(1L)).thenReturn(source);
        List<TextItemDO> sourceItems = Arrays.asList(
                item(11L, 1L, "contract.contractDetail.title", "合同详情"),
                item(12L, 1L, "contract.contractDetail.subtitle", "详情副标题"));
        when(textItemMapper.selectListByProfileId(1L)).thenReturn(sourceItems);

        service.cloneProfile(1L, "克隆文案");

        // 新建 profile：seedFrom 记录源 code，isActive 固定 false，textMode 继承自来源
        verify(textProfileMapper).insert(ArgumentMatchers.<TextProfileDO>argThat(d ->
                "克隆文案".equals(d.getName())
                        && d.getCode() != null && d.getCode().startsWith("text-")
                        && "text-1".equals(d.getSeedFrom())
                        && "offcial".equals(d.getTextMode())
                        && Boolean.FALSE.equals(d.getIsActive())));

        // 全部 items 复制一份，新 id（未设置）、指向新 profileId，且不是同一批 DO 实例
        verify(textItemMapper).insertBatch(argThat(list -> {
            @SuppressWarnings("unchecked")
            List<TextItemDO> copied = (List<TextItemDO>) list;
            if (copied.size() != 2) {
                return false;
            }
            boolean ok = true;
            for (int i = 0; i < copied.size(); i++) {
                TextItemDO c = copied.get(i);
                TextItemDO src = sourceItems.get(i);
                ok &= c != src;
                ok &= c.getId() == null;
                ok &= src.getItemKey().equals(c.getItemKey());
                ok &= src.getItemValue().equals(c.getItemValue());
                ok &= src.getPageKey().equals(c.getPageKey());
            }
            return ok;
        }));
    }

    // ── getActiveTextProfile ────────────────────────────────────
    @Test
    public void testGetActiveTextProfile_delegatesToMapperWithMode() {
        TextProfileDO active = profile(1L, true, "safe");
        when(textProfileMapper.selectActive("safe")).thenReturn(active);
        assertSame(active, service.getActiveTextProfile("safe"));
    }

    @Test
    public void testGetActiveTextProfile_offcialMode_delegatesWithOffcial() {
        TextProfileDO active = profile(2L, true, "offcial");
        when(textProfileMapper.selectActive("offcial")).thenReturn(active);
        assertSame(active, service.getActiveTextProfile("offcial"));
        verify(textProfileMapper, never()).selectActive("safe");
    }

    @Test
    public void testGetActiveTextProfile_notFound_returnsNull() {
        when(textProfileMapper.selectActive("safe")).thenReturn(null);
        assertNull(service.getActiveTextProfile("safe"));
    }

}
