package cn.iocoder.yudao.module.custom.service.contract;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.net.URI;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Prevent a writable contract from becoming a signing oracle for another user's private upload. */
@Component
public class ContractAttachmentReferenceGuard {
    private static final Pattern PRIVATE = Pattern.compile("^/admin-api/infra/file/[0-9]+/get/private/contracts/([0-9]+)/.+$");
    @Resource private SecurityFrameworkService security;

    public void validate(String requested, String previous) {
        if (StrUtil.isBlank(requested)) return;
        String path = path(requested);
        if (!path.contains("/get/private/contracts/")) return; // Existing public/legacy attachments remain supported.
        Matcher match = PRIVATE.matcher(path);
        if (!match.matches() || path.contains("..") || path.contains("//") || path.contains("\\") || path.contains("%")) {
            throw denied();
        }
        // An authorized editor may retain a counterparty's attachment on this same contract.
        if (StrUtil.isNotBlank(previous) && path.equals(path(previous))) return;
        Long actor = SecurityFrameworkUtils.getLoginUserId();
        if (actor == null) throw denied();
        if (String.valueOf(actor).equals(match.group(1))
                || security.hasAnyRoles("super_admin", "tenant_admin", "contract-manager")) return;
        throw denied();
    }
    private String path(String url) {
        try {
            String raw = new URI(url).getRawPath();
            if (raw == null) throw denied();
            return URLDecoder.decode(raw.replace("+", "%2B"), "UTF-8");
        } catch (Exception ex) { throw denied(); }
    }
    private AccessDeniedException denied() {
        return new AccessDeniedException("不能引用他人的私有合同附件，请重新上传");
    }
}
