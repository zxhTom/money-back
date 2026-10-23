package cn.iocoder.yudao.module.system.controller.admin.code;

/**
 * @author zxhtom
 * 11/22/25
 */
import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.urllink.GenerateUrlLinkRequest;
import cn.binarywang.wx.miniapp.constant.WxMaConstants;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import cn.iocoder.yudao.module.system.controller.admin.code.vo.QRCodeGenerator;
import cn.iocoder.yudao.module.system.controller.admin.code.vo.QRCodeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.PermitAll;

import java.util.UUID;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/api/qrcode")
public class QRCodeController {

    @Autowired
    WxMaService wxMaService;
    @Autowired
    private QRCodeGenerator qrCodeGenerator;

    @Autowired
    FileService fileService;

    /**
     * 生成二维码图片接口
     */
    @cn.iocoder.yudao.framework.ratelimiter.core.annotation.RateLimiter(time = 60, count = 60,
            keyResolver = cn.iocoder.yudao.framework.ratelimiter.core.keyresolver.impl.ClientIpRateLimiterKeyResolver.class)
    @GetMapping("/generateQRCodeUrl")
    @PermitAll
    @TenantIgnore
    public CommonResult<String> generateQRCodeUrl(
            @RequestParam String content,
            @RequestParam(defaultValue = "300") int width,
            @RequestParam(defaultValue = "300") int height) {

        QRCodeGenerator.validateRequest(content, width, height);
        try {
            try {
                int i = content.indexOf("?");
                // 1. 构建请求参数[citation:1]
                GenerateUrlLinkRequest request = GenerateUrlLinkRequest.builder()
                        .path(content.substring(0, i)) // 要跳转的小程序页面路径[citation:5]
                        .query(content.substring(i + 1))   // 页面参数，最大1024字符[citation:5]
                        .envVersion(WxMaConstants.DEFAULT_ENV_VERSION) // 默认"release"正式版[citation:1]
                        .expireType(1) // 失效类型：1-按间隔天数失效[citation:5]
                        .expireInterval(7) // 7天后链接失效，最长30天[citation:5]
                        .build();

                // 2. 调用SDK生成链接[citation:1]
                content = wxMaService.getLinkService().generateUrlLink(request);
            } catch (Exception e) {
                // 普通支付链接无法转换小程序链接时，保留原始二维码内容。
            }
            byte[] qrCodeImage = qrCodeGenerator.generateQRCodeImage(content, width, height);
            String file = fileService.createFile(qrCodeImage, UUID.randomUUID().toString(),
                    "demo", "png");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDispositionFormData("filename", "qrcode.png");
            return success(file);
        } catch (cn.iocoder.yudao.framework.common.exception.ServiceException e) {
            throw e;
        } catch (Exception e) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR.value(),"error");
        }
    }
    /**
     * 生成二维码图片接口
     */
    @cn.iocoder.yudao.framework.ratelimiter.core.annotation.RateLimiter(time = 60, count = 60,
            keyResolver = cn.iocoder.yudao.framework.ratelimiter.core.keyresolver.impl.ClientIpRateLimiterKeyResolver.class)
    @GetMapping("/generate")
    @PermitAll
    @TenantIgnore
    public CommonResult<byte[]> generateQRCode(
            @RequestParam String content,
            @RequestParam(defaultValue = "300") int width,
            @RequestParam(defaultValue = "300") int height) {

        QRCodeGenerator.validateRequest(content, width, height);
        try {
            byte[] qrCodeImage = qrCodeGenerator.generateQRCodeImage(content, width, height);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("filename", "qrcode.png");

            return success(qrCodeImage);
        } catch (cn.iocoder.yudao.framework.common.exception.ServiceException e) {
            throw e;
        } catch (Exception e) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR.value(),"");
        }
    }

    /**
     * 生成Base64格式的二维码
     */
    @cn.iocoder.yudao.framework.ratelimiter.core.annotation.RateLimiter(time = 60, count = 60,
            keyResolver = cn.iocoder.yudao.framework.ratelimiter.core.keyresolver.impl.ClientIpRateLimiterKeyResolver.class)
    @GetMapping("/generate-base64")
    public CommonResult<String> generateQRCodeBase64(
            @RequestParam String content,
            @RequestParam(defaultValue = "300") int width,
            @RequestParam(defaultValue = "300") int height) {

        QRCodeGenerator.validateRequest(content, width, height);
        try {
            byte[] qrCodeImage = qrCodeGenerator.generateQRCodeImage(content, width, height);
            String base64Image = java.util.Base64.getEncoder().encodeToString(qrCodeImage);
            String result = "data:image/png;base64," + base64Image;

            return success(result);
        } catch (cn.iocoder.yudao.framework.common.exception.ServiceException e) {
            throw e;
        } catch (Exception e) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR.value(),"");
        }
    }

    /**
     * 生成带文本描述的二维码
     */
    @cn.iocoder.yudao.framework.ratelimiter.core.annotation.RateLimiter(time = 60, count = 60,
            keyResolver = cn.iocoder.yudao.framework.ratelimiter.core.keyresolver.impl.ClientIpRateLimiterKeyResolver.class)
    @PostMapping("/generate-with-text")
    public CommonResult<byte[]> generateQRCodeWithText(@RequestBody QRCodeRequest request) {
        QRCodeGenerator.validateRequest(request.getContent(), request.getWidth(), request.getHeight());
        try {
            byte[] qrCodeImage = qrCodeGenerator.generateQRCodeImage(
                    request.getContent(),
                    request.getWidth(),
                    request.getHeight()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);

            return success(qrCodeImage);
        } catch (cn.iocoder.yudao.framework.common.exception.ServiceException e) {
            throw e;
        } catch (Exception e) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR.value(),"");
        }
    }
}
