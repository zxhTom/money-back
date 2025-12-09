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

    /**
     * 生成二维码字节数组
     * @param content 二维码内容
     * @param width 宽度
     * @param height 高度
     * @return 二维码图片的字节数组
     */
    public byte[] generateQRCodeImage(String content, int width, int height) {
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
            throw new RuntimeException("生成二维码失败: " + e.getMessage(), e);
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
