package cn.iocoder.yudao.module.custom.controller.admin.text;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.custom.controller.admin.text.vo.TextAppRespVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.text.TextItemDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.text.TextProfileDO;
import cn.iocoder.yudao.module.custom.service.text.TextItemService;
import cn.iocoder.yudao.module.custom.service.text.TextProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * mode 参数的 fail-closed 行为：缺失/非法值一律按 safe 处理，只有精确等于 "offcial" 才透传 offcial。
 */
@ExtendWith(MockitoExtension.class)
public class TextAppControllerTest {

    @Mock
    private TextProfileService textProfileService;

    @Mock
    private TextItemService textItemService;

    @InjectMocks
    private TextAppController controller;

    private TextProfileDO profile(String code) {
        TextProfileDO d = new TextProfileDO();
        d.setId(1L);
        d.setCode(code);
        return d;
    }

    @Test
    public void testGetCurrentText_defaultParam_usesSafe() {
        // 未传 mode 时 Spring 会用 @RequestParam 的 defaultValue="safe"
        when(textProfileService.getActiveTextProfile("safe")).thenReturn(profile("default-safe"));
        when(textItemService.listByProfile(1L)).thenReturn(Collections.<TextItemDO>emptyList());

        CommonResult<TextAppRespVO> result = controller.getCurrentText("safe");

        assertEquals("default-safe", result.getData().getProfileCode());
        verify(textProfileService).getActiveTextProfile("safe");
        verify(textProfileService, never()).getActiveTextProfile("offcial");
    }

    @Test
    public void testGetCurrentText_offcialMode_passesThroughOffcial() {
        when(textProfileService.getActiveTextProfile("offcial")).thenReturn(profile("default-offcial"));
        when(textItemService.listByProfile(1L)).thenReturn(Collections.<TextItemDO>emptyList());

        CommonResult<TextAppRespVO> result = controller.getCurrentText("offcial");

        assertEquals("default-offcial", result.getData().getProfileCode());
        verify(textProfileService).getActiveTextProfile("offcial");
    }

    @Test
    public void testGetCurrentText_illegalValue_failsClosedToSafe() {
        when(textProfileService.getActiveTextProfile("safe")).thenReturn(profile("default-safe"));
        when(textItemService.listByProfile(1L)).thenReturn(Collections.<TextItemDO>emptyList());

        CommonResult<TextAppRespVO> result = controller.getCurrentText("not-a-real-mode");

        assertEquals("default-safe", result.getData().getProfileCode());
        verify(textProfileService).getActiveTextProfile("safe");
        verify(textProfileService, never()).getActiveTextProfile("not-a-real-mode");
    }

    @Test
    public void testGetCurrentText_emptyValue_failsClosedToSafe() {
        when(textProfileService.getActiveTextProfile("safe")).thenReturn(profile("default-safe"));
        when(textItemService.listByProfile(1L)).thenReturn(Collections.<TextItemDO>emptyList());

        CommonResult<TextAppRespVO> result = controller.getCurrentText("");

        assertEquals("default-safe", result.getData().getProfileCode());
        verify(textProfileService).getActiveTextProfile("safe");
    }

    @Test
    public void testGetCurrentText_noActiveProfileForMode_returnsNullCodeAndEmptyTexts() {
        when(textProfileService.getActiveTextProfile("offcial")).thenReturn(null);

        CommonResult<TextAppRespVO> result = controller.getCurrentText("offcial");

        assertNull(result.getData().getProfileCode());
        assertTrue(result.getData().getTexts().isEmpty());
        verify(textItemService, never()).listByProfile(anyLong());
    }

}
