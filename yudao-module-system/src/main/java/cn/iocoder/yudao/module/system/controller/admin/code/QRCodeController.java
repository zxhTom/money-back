package cn.iocoder.yudao.module.system.controller.admin.code;

/**
 * @author zxhtom
 * 11/22/25
 */
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
    private QRCodeGenerator qrCodeGenerator;

    @Autowired
    FileService fileService;

    /**
     * 生成二维码图片接口
     */
    @GetMapping("/generateQRCodeUrl")
    @PermitAll
    @TenantIgnore
    public CommonResult<String> generateQRCodeUrl(
            @RequestParam String content,
            @RequestParam(defaultValue = "300") int width,
            @RequestParam(defaultValue = "300") int height) {

        try {
            byte[] qrCodeImage = qrCodeGenerator.generateQRCodeImage(content, width, height);
            String file = fileService.createFile(qrCodeImage, UUID.randomUUID().toString(),
                    "demo", "png");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDispositionFormData("filename", "qrcode.png");
            return success(file);
        } catch (Exception e) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR.value(),"error");
        }
    }
    /**
     * 生成二维码图片接口
     */
    @GetMapping("/generate")
    @PermitAll
    @TenantIgnore
    public CommonResult<byte[]> generateQRCode(
            @RequestParam String content,
            @RequestParam(defaultValue = "300") int width,
            @RequestParam(defaultValue = "300") int height) {

        try {
            byte[] qrCodeImage = qrCodeGenerator.generateQRCodeImage(content, width, height);
            String file = fileService.createFile(qrCodeImage, UUID.randomUUID().toString(),
                    "demo", "png");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("filename", "qrcode.png");

            return success(qrCodeImage);
        } catch (Exception e) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR.value(),"");
        }
    }

    /**
     * 生成Base64格式的二维码
     */
    @GetMapping("/generate-base64")
    public CommonResult<String> generateQRCodeBase64(
            @RequestParam String content,
            @RequestParam(defaultValue = "300") int width,
            @RequestParam(defaultValue = "300") int height) {

        try {
            byte[] qrCodeImage = qrCodeGenerator.generateQRCodeImage(content, width, height);
            String base64Image = java.util.Base64.getEncoder().encodeToString(qrCodeImage);
            String result = "data:image/png;base64," + base64Image;

            return success(result);
        } catch (Exception e) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR.value(),"");
        }
    }

    /**
     * 生成带文本描述的二维码
     */
    @PostMapping("/generate-with-text")
    public CommonResult<byte[]> generateQRCodeWithText(@RequestBody QRCodeRequest request) {
        try {
            byte[] qrCodeImage = qrCodeGenerator.generateQRCodeImage(
                    request.getContent(),
                    request.getWidth(),
                    request.getHeight()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);

            return success(qrCodeImage);
        } catch (Exception e) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR.value(),"");
        }
    }
}
