package cn.iocoder.yudao.module.infra.framework.file.core.audit;

/**
 * 文件内容审核 SPI，由具体渠道（如微信小程序 mediaCheckAsync）实现。
 * infra 模块不感知具体实现，避免依赖微信 SDK。
 */
public interface FileContentAuditor {

    /**
     * 提交图片做异步内容检测。
     *
     * @param url     图片公网可访问地址
     * @param content 图片内容
     * @return 检测任务 traceId；返回 null 表示未提交（无需/无法审核），文件按通过处理
     */
    String submitImageCheck(String url, byte[] content);

    /**
     * 同步检测文本是否含违规内容。
     *
     * @param text 文本
     * @return true=通过；false=含违规
     */
    boolean checkText(String text);
}
