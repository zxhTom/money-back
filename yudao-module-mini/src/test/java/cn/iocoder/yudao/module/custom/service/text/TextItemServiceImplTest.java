package cn.iocoder.yudao.module.custom.service.text;

import cn.iocoder.yudao.module.custom.controller.admin.text.vo.TextItemBatchUpdateReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.text.vo.TextItemSaveReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.text.TextItemDO;
import cn.iocoder.yudao.module.custom.dal.mysql.text.TextItemMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TextItemServiceImplTest {

    @Mock
    private TextItemMapper textItemMapper;

    @InjectMocks
    private TextItemServiceImpl service;

    private TextItemDO existingItem(Long id, String key, String value) {
        TextItemDO d = new TextItemDO();
        d.setId(id);
        d.setProfileId(1L);
        d.setPageKey("contract.contractDetail");
        d.setItemKey(key);
        d.setItemValue(value);
        d.setSort(1);
        return d;
    }

    private TextItemSaveReqVO saveReqVO(String key, String value) {
        TextItemSaveReqVO vo = new TextItemSaveReqVO();
        vo.setItemKey(key);
        vo.setItemValue(value);
        return vo;
    }

    // ── listByProfile / listByProfileAndPage / searchByKeyword ──
    @Test
    public void testListByProfile_delegatesToMapper() {
        List<TextItemDO> items = Arrays.asList(existingItem(1L, "a", "1"), existingItem(2L, "b", "2"));
        when(textItemMapper.selectListByProfileId(1L)).thenReturn(items);
        assertSame(items, service.listByProfile(1L));
    }

    @Test
    public void testListByProfileAndPage_delegatesToMapper() {
        List<TextItemDO> items = Arrays.asList(existingItem(1L, "a", "1"));
        when(textItemMapper.selectListByProfileIdAndPageKey(1L, "contract.contractDetail")).thenReturn(items);
        assertSame(items, service.listByProfileAndPage(1L, "contract.contractDetail"));
    }

    @Test
    public void testSearchByKeyword_delegatesToMapper() {
        List<TextItemDO> items = Arrays.asList(existingItem(1L, "a", "1"));
        when(textItemMapper.selectListByProfileIdAndKeyword(1L, "标题")).thenReturn(items);
        assertSame(items, service.searchByKeyword(1L, "标题"));
    }

    // ── batchUpdate: upsert ─────────────────────────────────────
    @Test
    public void testBatchUpdate_allNew_insertsAll() {
        when(textItemMapper.selectByProfileIdAndItemKey(eq(1L), anyString())).thenReturn(null);

        TextItemBatchUpdateReqVO reqVO = new TextItemBatchUpdateReqVO();
        reqVO.setProfileId(1L);
        reqVO.setPageKey("contract.contractDetail");
        reqVO.setItems(Arrays.asList(
                saveReqVO("contract.contractDetail.title", "合同详情"),
                saveReqVO("contract.contractDetail.subtitle", "副标题")));

        service.batchUpdate(reqVO);

        verify(textItemMapper, times(2)).insert(ArgumentMatchers.<TextItemDO>argThat(d ->
                d.getProfileId().equals(1L) && "contract.contractDetail".equals(d.getPageKey())));
        verify(textItemMapper, never()).updateById(ArgumentMatchers.<TextItemDO>any());
    }

    @Test
    public void testBatchUpdate_allExisting_updatesAll() {
        TextItemDO existing1 = existingItem(11L, "contract.contractDetail.title", "旧标题");
        TextItemDO existing2 = existingItem(12L, "contract.contractDetail.subtitle", "旧副标题");
        when(textItemMapper.selectByProfileIdAndItemKey(1L, "contract.contractDetail.title")).thenReturn(existing1);
        when(textItemMapper.selectByProfileIdAndItemKey(1L, "contract.contractDetail.subtitle")).thenReturn(existing2);

        TextItemBatchUpdateReqVO reqVO = new TextItemBatchUpdateReqVO();
        reqVO.setProfileId(1L);
        reqVO.setPageKey("contract.contractDetail");
        reqVO.setItems(Arrays.asList(
                saveReqVO("contract.contractDetail.title", "新标题"),
                saveReqVO("contract.contractDetail.subtitle", "新副标题")));

        service.batchUpdate(reqVO);

        verify(textItemMapper, never()).insert(ArgumentMatchers.<TextItemDO>any());
        verify(textItemMapper).updateById(ArgumentMatchers.<TextItemDO>argThat(d ->
                d.getId().equals(11L) && "新标题".equals(d.getItemValue())));
        verify(textItemMapper).updateById(ArgumentMatchers.<TextItemDO>argThat(d ->
                d.getId().equals(12L) && "新副标题".equals(d.getItemValue())));
    }

    @Test
    public void testBatchUpdate_mixedNewAndExisting() {
        TextItemDO existing = existingItem(11L, "contract.contractDetail.title", "旧标题");
        when(textItemMapper.selectByProfileIdAndItemKey(1L, "contract.contractDetail.title")).thenReturn(existing);
        when(textItemMapper.selectByProfileIdAndItemKey(1L, "contract.contractDetail.subtitle")).thenReturn(null);

        TextItemBatchUpdateReqVO reqVO = new TextItemBatchUpdateReqVO();
        reqVO.setProfileId(1L);
        reqVO.setPageKey("contract.contractDetail");
        reqVO.setItems(Arrays.asList(
                saveReqVO("contract.contractDetail.title", "新标题"),
                saveReqVO("contract.contractDetail.subtitle", "新副标题")));

        service.batchUpdate(reqVO);

        verify(textItemMapper).updateById(ArgumentMatchers.<TextItemDO>argThat(d ->
                d.getId().equals(11L) && "新标题".equals(d.getItemValue())));
        verify(textItemMapper).insert(ArgumentMatchers.<TextItemDO>argThat(d ->
                d.getId() == null && "contract.contractDetail.subtitle".equals(d.getItemKey())
                        && "新副标题".equals(d.getItemValue()) && d.getProfileId().equals(1L)
                        && "contract.contractDetail".equals(d.getPageKey())));
    }

    @Test
    public void testBatchUpdate_emptyItems_noop() {
        TextItemBatchUpdateReqVO reqVO = new TextItemBatchUpdateReqVO();
        reqVO.setProfileId(1L);
        reqVO.setPageKey("contract.contractDetail");
        reqVO.setItems(new ArrayList<>());

        service.batchUpdate(reqVO);

        verify(textItemMapper, never()).insert(ArgumentMatchers.<TextItemDO>any());
        verify(textItemMapper, never()).updateById(ArgumentMatchers.<TextItemDO>any());
    }

}
