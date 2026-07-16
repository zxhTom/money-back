package cn.iocoder.yudao.module.infra.service.file;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.file.FileCreateReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.file.FilePageReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.file.FilePresignedUrlRespVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;
import cn.iocoder.yudao.module.infra.dal.mysql.file.FileMapper;
import cn.iocoder.yudao.module.infra.framework.file.core.audit.FileContentAuditor;
import cn.iocoder.yudao.module.infra.framework.file.core.client.FileClient;
import cn.iocoder.yudao.module.infra.framework.file.core.utils.FileTypeUtils;
import com.google.common.annotations.VisibleForTesting;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

import static cn.hutool.core.date.DatePattern.PURE_DATE_PATTERN;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.FILE_NOT_EXISTS;

/**
 * 文件 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class FileServiceImpl implements FileService {

    /**
     * 上传文件的前缀，是否包含日期（yyyyMMdd）
     *
     * 目的：按照日期，进行分目录
     */
    static boolean PATH_PREFIX_DATE_ENABLE = true;
    /**
     * 上传文件的后缀，是否包含时间戳
     *
     * 目的：保证文件的唯一性，避免覆盖
     * 定制：可按需调整成 UUID、或者其他方式
     */
    static boolean PATH_SUFFIX_TIMESTAMP_ENABLE = true;

    @Resource
    private FileConfigService fileConfigService;

    @Resource
    private FileMapper fileMapper;

    @Resource
    private ObjectProvider<FileContentAuditor> auditorProvider;

    @Override
    public PageResult<FileDO> getFilePage(FilePageReqVO pageReqVO) {
        return fileMapper.selectPage(pageReqVO);
    }

    /** 禁止上传的危险扩展名（服务端可执行 / 浏览器可执行脚本 / 可部署包 / 可执行程序） */
    private static final java.util.Set<String> BLOCKED_UPLOAD_EXT = new java.util.HashSet<>(java.util.Arrays.asList(
            "jsp", "jspx", "jspf", "jspa", "jsv", "jsw", "jhtml", "jtml",
            "php", "php3", "php4", "php5", "php7", "phtml", "pht", "phar",
            "asp", "aspx", "ashx", "asmx", "ascx", "cer", "cshtml",
            "war", "jar", "ear", "class", "cgi", "pl", "py", "rb",
            "sh", "bash", "ksh", "bat", "cmd", "com", "exe", "dll", "msi", "scr",
            "vbs", "vbe", "js", "mjs", "wsf", "wsh", "ps1", "psm1", "hta",
            "svg", "html", "htm", "xhtml", "shtml", "dhtml", "swf"));
    private static final long MAX_UPLOAD_SIZE_BYTES = 30L * 1024 * 1024;

    /** 上传安全校验：危险扩展名黑名单 + 内容签名 + 大小 + 文件名净化，拦截 jsp/html/war 等上传攻击。 */
    private void validateUploadSecurity(byte[] content, String name) {
        if (content == null || content.length == 0) {
            throw cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0(400, "文件内容为空");
        }
        if (content.length > MAX_UPLOAD_SIZE_BYTES) {
            throw cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0(400, "文件超过大小限制(30MB)");
        }
        String fname = name == null ? "" : name.trim();
        if (fname.contains("/") || fname.contains("\\") || fname.contains("..")
                || fname.chars().anyMatch(c -> c < 0x20)) {
            throw cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0(400, "非法文件名");
        }
        String ext = cn.hutool.core.io.FileUtil.extName(fname).toLowerCase();
        if (BLOCKED_UPLOAD_EXT.contains(ext)) {
            throw cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0(400, "不允许上传该类型文件：." + ext);
        }
        // 内容签名：即便改了后缀，内容以脚本/网页开头也拒绝（挡改名绕过）
        String head = new String(content, 0, Math.min(content.length, 512),
                java.nio.charset.StandardCharsets.ISO_8859_1).trim().toLowerCase();
        for (String sig : new String[]{"<script", "<%", "<?php", "<?=", "<!doctype html", "<html", "<svg", "<iframe"}) {
            if (head.startsWith(sig)) {
                throw cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0(400, "文件内容疑似脚本/网页，已拒绝上传");
            }
        }
    }

    @Override
    @SneakyThrows
    public String createFile(byte[] content, String name, String directory, String type) {
        // 0. 上传安全校验（防 jsp/html/war/exe 等上传攻击）
        validateUploadSecurity(content, name);
        // 1.1 处理 type 为空的情况
        if (StrUtil.isEmpty(type)) {
            type = FileTypeUtils.getMineType(content, name);
        }
        // 1.2 处理 name 为空的情况
        if (StrUtil.isEmpty(name)) {
            name = DigestUtil.sha256Hex(content);
        }
        if (StrUtil.isEmpty(FileUtil.extName(name))) {
            // 如果 name 没有后缀 type，则补充后缀
            String extension = FileTypeUtils.getExtension(type);
            if (StrUtil.isNotEmpty(extension)) {
                name = name + extension;
            }
        }

        // 2.1 生成上传的 path，需要保证唯一
        String path = generateUploadPath(name, directory);
        // 2.2 上传到文件存储器
        FileClient client = fileConfigService.getMasterFileClient();
        Assert.notNull(client, "客户端(master) 不能为空");
        String url = client.upload(content, path, type);

        // 3. 保存到数据库
        FileDO file = new FileDO().setConfigId(client.getId())
                .setName(name).setPath(path).setUrl(url)
                .setType(type).setSize(content.length);
        fileMapper.insert(file);

        // 4. 图片内容安全检测（微信 mediaCheckAsync，异步回调更新状态）
        submitContentAudit(file, content, type);
        return url;
    }

    private void submitContentAudit(FileDO file, byte[] content, String type) {
        if (type == null || !StrUtil.startWithIgnoreCase(type, "image/")) {
            return;
        }
        FileContentAuditor auditor = auditorProvider.getIfAvailable();
        if (auditor == null) {
            return;
        }
        try {
            String traceId = auditor.submitImageCheck(file.getUrl(), content);
            if (StrUtil.isNotBlank(traceId)) {
                fileMapper.updateById(new FileDO().setId(file.getId())
                        .setAuditStatus(0).setAuditTraceId(traceId));
            }
        } catch (Exception ex) {
            log.error("[submitContentAudit][file({}) 提交内容检测失败]", file.getId(), ex);
        }
    }

    @Override
    public void updateAuditByTraceId(String traceId, boolean pass) {
        if (StrUtil.isBlank(traceId)) {
            return;
        }
        FileDO file = fileMapper.selectByTraceId(traceId);
        if (file == null) {
            log.warn("[updateAuditByTraceId][traceId({}) 未匹配到文件]", traceId);
            return;
        }
        if (pass) {
            fileMapper.updateById(new FileDO().setId(file.getId()).setAuditStatus(1));
            return;
        }
        // 违规：删除存储中的文件 + 标记违规
        try {
            FileClient client = fileConfigService.getFileClient(file.getConfigId());
            if (client != null) {
                client.delete(file.getPath());
            }
        } catch (Exception ex) {
            log.error("[updateAuditByTraceId][file({}) 删除违规文件失败]", file.getId(), ex);
        }
        fileMapper.updateById(new FileDO().setId(file.getId()).setAuditStatus(2));
        log.warn("[updateAuditByTraceId][file({}) path({}) 内容违规，已删除]", file.getId(), file.getPath());
    }

    @VisibleForTesting
    String generateUploadPath(String name, String directory) {
        // 1. 生成前缀、后缀
        String prefix = null;
        if (PATH_PREFIX_DATE_ENABLE) {
            prefix = LocalDateTimeUtil.format(LocalDateTimeUtil.now(), PURE_DATE_PATTERN);
        }
        String suffix = null;
        if (PATH_SUFFIX_TIMESTAMP_ENABLE) {
            suffix = String.valueOf(System.currentTimeMillis());
        }

        // 2.1 先拼接 suffix 后缀
        if (StrUtil.isNotEmpty(suffix)) {
            String ext = FileUtil.extName(name);
            if (StrUtil.isNotEmpty(ext)) {
                name = FileUtil.mainName(name) + StrUtil.C_UNDERLINE + suffix + StrUtil.DOT + ext;
            } else {
                name = name + StrUtil.C_UNDERLINE + suffix;
            }
        }
        // 2.2 再拼接 prefix 前缀
        if (StrUtil.isNotEmpty(prefix)) {
            name = prefix + StrUtil.SLASH + name;
        }
        // 2.3 最后拼接 directory 目录
        if (StrUtil.isNotEmpty(directory)) {
            name = directory + StrUtil.SLASH + name;
        }
        return name;
    }

    @Override
    @SneakyThrows
    public FilePresignedUrlRespVO presignPutUrl(String name, String directory) {
        // 1. 生成上传的 path，需要保证唯一
        String path = generateUploadPath(name, directory);

        // 2. 获取文件预签名地址
        FileClient fileClient = fileConfigService.getMasterFileClient();
        String uploadUrl = fileClient.presignPutUrl(path);
        String visitUrl = fileClient.presignGetUrl(path, null);
        return new FilePresignedUrlRespVO().setConfigId(fileClient.getId())
                .setPath(path).setUploadUrl(uploadUrl).setUrl(visitUrl);
    }

    @Override
    public String presignGetUrl(String url, Integer expirationSeconds) {
        FileClient fileClient = fileConfigService.getMasterFileClient();
        return fileClient.presignGetUrl(url, expirationSeconds);
    }

    @Override
    public Long createFile(FileCreateReqVO createReqVO) {
        createReqVO.setUrl(HttpUtils.removeUrlQuery(createReqVO.getUrl())); // 目的：移除私有桶情况下，URL 的签名参数
        FileDO file = BeanUtils.toBean(createReqVO, FileDO.class);
        fileMapper.insert(file);
        return file.getId();
    }

    @Override
    public void deleteFile(Long id) throws Exception {
        // 校验存在
        FileDO file = validateFileExists(id);

        // 从文件存储器中删除
        FileClient client = fileConfigService.getFileClient(file.getConfigId());
        Assert.notNull(client, "客户端({}) 不能为空", file.getConfigId());
        client.delete(file.getPath());

        // 删除记录
        fileMapper.deleteById(id);
    }

    @Override
    @SneakyThrows
    public void deleteFileList(List<Long> ids) {
        // 删除文件
        List<FileDO> files = fileMapper.selectByIds(ids);
        for (FileDO file : files) {
            // 获取客户端
            FileClient client = fileConfigService.getFileClient(file.getConfigId());
            Assert.notNull(client, "客户端({}) 不能为空", file.getPath());
            // 删除文件
            client.delete(file.getPath());
        }

        // 删除记录
        fileMapper.deleteByIds(ids);
    }

    private FileDO validateFileExists(Long id) {
        FileDO fileDO = fileMapper.selectById(id);
        if (fileDO == null) {
            throw exception(FILE_NOT_EXISTS);
        }
        return fileDO;
    }

    @Override
    public byte[] getFileContent(Long configId, String path) throws Exception {
        FileClient client = fileConfigService.getFileClient(configId);
        Assert.notNull(client, "客户端({}) 不能为空", configId);
        return client.getContent(path);
    }

}
