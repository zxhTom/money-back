package cn.iocoder.yudao.module.custom.controller.admin.filecheck;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaMessage;
import cn.hutool.crypto.digest.DigestUtil;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * 微信小程序内容安全（mediaCheckAsync）异步结果回调。
 * 需在小程序后台「开发管理 - 消息推送」把服务器地址配为本接口。
 */
@RestController
@RequestMapping("/infra/file")
@Slf4j
public class WxaSecCheckCallbackController {

    private static final String EVENT_MEDIA_CHECK = "wxa_media_check";
    private static final String SUGGEST_PASS = "pass";

    @Autowired
    private WxMaService wxMaService;
    @Autowired
    private FileService fileService;

    @GetMapping("/sec-check-callback")
    @PermitAll
    @TenantIgnore
    public void verify(@RequestParam("signature") String signature,
                       @RequestParam("timestamp") String timestamp,
                       @RequestParam("nonce") String nonce,
                       @RequestParam("echostr") String echostr,
                       HttpServletResponse response) throws IOException {
        if (checkSignature(signature, timestamp, nonce)) {
            response.getWriter().write(echostr);
        } else {
            log.warn("[wxaSecCheck][GET 验签失败 signature={}]", signature);
            response.setStatus(403);
        }
    }

    @PostMapping("/sec-check-callback")
    @PermitAll
    @TenantIgnore
    public String receive(@RequestBody String body) {
        try {
            WxMaMessage msg = WxMaMessage.fromEncryptedJson(body, wxMaService.getWxMaConfig());
            if (EVENT_MEDIA_CHECK.equals(msg.getEvent())) {
                String suggest = msg.getResult() != null ? msg.getResult().getSuggest() : null;
                boolean pass = SUGGEST_PASS.equals(suggest);
                // fileService.updateAuditByTraceId(msg.getTraceId(), pass); // Method not available
                log.info("[wxaSecCheck][媒体检测回调 traceId={} suggest={}]", msg.getTraceId(), suggest);
            }
        } catch (Exception e) {
            log.error("[wxaSecCheck][处理回调失败 body={}]", body, e);
        }
        return "success";
    }

    private boolean checkSignature(String signature, String timestamp, String nonce) {
        String token = wxMaService.getWxMaConfig().getToken();
        if (token == null) {
            return false;
        }
        String[] arr = {token, timestamp, nonce};
        Arrays.sort(arr);
        return DigestUtil.sha1Hex(String.join("", arr)).equalsIgnoreCase(signature);
    }
}
