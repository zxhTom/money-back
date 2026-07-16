package cn.iocoder.yudao.module.custom.framework.filecheck;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaMediaAsyncCheckResult;
import cn.binarywang.wx.miniapp.bean.security.WxMaMediaSecCheckCheckRequest;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.custom.dal.dataobject.wechat.MiniUserDo;
import cn.iocoder.yudao.module.custom.dal.mysql.wechat.MiniUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 基于微信小程序内容安全接口的文件内容审核实现。
 * 图片走 mediaCheckAsync（异步，回调更新状态），文本走 checkMessage（同步）。
 */
@Component
@Slf4j
public class WxMaFileContentAuditor {

    private static final int MEDIA_TYPE_IMAGE = 2;
    private static final int SCENE_PROFILE = 1;

    @Autowired
    private WxMaService wxMaService;
    @Autowired
    private MiniUserMapper miniUserMapper;

    public String submitImageCheck(String url, byte[] content) {
        if (StrUtil.isBlank(url)) {
            return null;
        }
        String openid = resolveOpenid();
        if (StrUtil.isBlank(openid)) {
            log.warn("[submitImageCheck][当前用户无 openid，跳过图片内容检测 url={}]", url);
            return null;
        }
        try {
            WxMaMediaSecCheckCheckRequest req = WxMaMediaSecCheckCheckRequest.builder()
                    .mediaUrl(url).mediaType(MEDIA_TYPE_IMAGE).version(2)
                    .scene(SCENE_PROFILE).openid(openid).build();
            WxMaMediaAsyncCheckResult result = wxMaService.getSecurityService().mediaCheckAsync(req);
            return result != null ? result.getTraceId() : null;
        } catch (Exception e) {
            log.error("[submitImageCheck][提交图片内容检测失败 url={}]", url, e);
            return null;
        }
    }

    public boolean checkText(String text) {
        if (StrUtil.isBlank(text)) {
            return true;
        }
        try {
            return wxMaService.getSecurityService().checkMessage(text);
        } catch (Exception e) {
            log.error("[checkText][文本内容检测异常，放行]", e);
            return true;
        }
    }

    private String resolveOpenid() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            return null;
        }
        List<MiniUserDo> list = miniUserMapper.selectList(MiniUserDo::getUserId, userId);
        for (MiniUserDo user : list) {
            if (StrUtil.isNotBlank(user.getOpenId())) {
                return user.getOpenId();
            }
        }
        return null;
    }
}
