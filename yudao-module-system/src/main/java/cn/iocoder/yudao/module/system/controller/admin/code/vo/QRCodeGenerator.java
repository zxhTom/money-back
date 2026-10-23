package cn.iocoder.yudao.module.system.controller.admin.code.vo;

/**
 * @author zxhtom
 * 11/22/25
 */

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Component
public class QRCodeGenerator {
    private static final java.util.concurrent.Semaphore ENCODERS = new java.util.concurrent.Semaphore(4);

    public static void validateRequest(String content, int width, int height) {
        if (content == null || content.length() > 2048 || content.trim().isEmpty()
                || content.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 2048
                || width < 64 || height < 64 || width > 1024 || height > 1024
                || (long) width * height > 1048576L) {
            throw cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0(400,
                    "二维码内容不能为空且不超过2048字节，宽高需在64至1024像素之间");
        }
    }

    /**
     * 生成二维码字节数组
     * @param content 二维码内容
     * @param width 宽度
     * @param height 高度
     * @return 二维码图片的字节数组
     */
    public byte[] generateQRCodeImage(String content, int width, int height) {
        validateRequest(content, width, height);
        if (!ENCODERS.tryAcquire()) throw cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0(429, "二维码生成繁忙，请稍后再试");
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("生成二维码失败", e);
        } finally {
            ENCODERS.release();
        }
    }

    /**
     * 生成带Logo的二维码
     */
    public byte[] generateQRCodeWithLogo(String content, int width, int height, byte[] logoBytes) {
        try {
            // 先生成基础二维码
            byte[] qrCodeBytes = generateQRCodeImage(content, width, height);
            BufferedImage qrCodeImage = ImageIO.read(new java.io.ByteArrayInputStream(qrCodeBytes));
            BufferedImage logoImage = ImageIO.read(new java.io.ByteArrayInputStream(logoBytes));

            // 在二维码中央添加Logo
            Graphics2D graphics = qrCodeImage.createGraphics();
            int logoWidth = width / 5;
            int logoHeight = height / 5;
            int x = (width - logoWidth) / 2;
            int y = (height - logoHeight) / 2;

            graphics.drawImage(logoImage, x, y, logoWidth, logoHeight, null);
            graphics.dispose();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(qrCodeImage, "PNG", outputStream);

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("生成带Logo的二维码失败: " + e.getMessage(), e);
        }
    }

    /**
     * 保存二维码到文件
     */
    public void saveQRCodeToFile(String content, int width, int height, String filePath) {
        try {
            byte[] qrCodeBytes = generateQRCodeImage(content, width, height);
            File outputFile = new File(filePath);
            java.nio.file.Files.write(outputFile.toPath(), qrCodeBytes);
        } catch (Exception e) {
            throw new RuntimeException("保存二维码文件失败: " + e.getMessage(), e);
        }
    }
}
