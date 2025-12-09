package cn.iocoder.yudao.module.system.controller.admin.code.vo;

/**
 * 请求参数封装类
 */
public class QRCodeRequest {
    private String content;
    private int width = 300;
    private int height = 300;

    // getter 和 setter 方法
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
}
