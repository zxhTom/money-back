package cn.iocoder.yudao.module.custom.service.feedback;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.custom.dal.dataobject.feedback.FeedbackDO;
import cn.iocoder.yudao.module.system.dal.dataobject.notify.NotifyMessageDO;
import cn.iocoder.yudao.module.system.dal.mysql.notify.NotifyMessageMapper;
import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
@Slf4j
public class FeedbackNotifyService {
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private NotifyMessageMapper notifyMessageMapper;
    @Resource
    private cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService security;

    private static final String ADMIN_SQL = "SELECT DISTINCT u.id FROM system_users u "
            + "JOIN system_user_role ur ON ur.user_id = u.id AND ur.tenant_id = u.tenant_id AND ur.deleted = 0 "
            + "JOIN system_role r ON r.id = ur.role_id AND r.tenant_id = u.tenant_id AND r.deleted = 0 "
            + "WHERE u.tenant_id = ? AND u.deleted = 0 AND u.status = 0 AND r.status = 0 "
            + "AND r.code IN ('super_admin', 'tenant_admin')";

    public List<Long> getAdminIds() {
        return jdbcTemplate.queryForList(ADMIN_SQL, Long.class, TenantContextHolder.getRequiredTenantId());
    }

    public Config getConfig() {
        requireAdmin();
        return readConfig();
    }

    public void saveConfig(Config config) {
        requireAdmin();
        if (config == null || config.getEnabled() == null || config.getRecipientUserIds() == null) {
            throw new IllegalArgumentException("通知配置不完整");
        }
        List<Long> ids = config.getRecipientUserIds().stream().distinct().collect(Collectors.toList());
        if (ids.size() > 50 || ids.contains(null) || !getAdminIds().containsAll(ids)) {
            throw new IllegalArgumentException("接收人必须是当前租户的有效管理员，最多 50 人");
        }
        if (config.getEnabled() && ids.isEmpty()) {
            throw new IllegalArgumentException("启用通知时请选择至少一位管理员");
        }
        jdbcTemplate.update("INSERT INTO system_feedback_notify_config (tenant_id, enabled, recipient_user_ids) VALUES (?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE enabled = VALUES(enabled), recipient_user_ids = VALUES(recipient_user_ids)",
                TenantContextHolder.getRequiredTenantId(), config.getEnabled() ? 1 : 0, JSON.toJSONString(ids));
    }

    public void notifyNewFeedback(FeedbackDO feedback) {
        try {
            Config config = readConfig();
            if (!config.getEnabled()) return;
            // 保存后重新校验角色和租户，防止已离职或角色已撤销的用户继续收到提醒。
            List<Long> eligible = getAdminIds();
            for (Long id : config.getRecipientUserIds()) {
                if (!eligible.contains(id)) continue;
                String content = "收到新的" + feedback.getType() + "，反馈编号 #" + feedback.getId() + "，请前往反馈列表查看。";
                jdbcTemplate.update("INSERT INTO system_notify_message "
                                + "(user_id, user_type, template_id, template_code, template_type, template_nickname, "
                                + "template_content, template_params, read_status, tenant_id) "
                                + "VALUES (?, 2, 0, 'FEEDBACK_NEW', 3, '帮助与反馈', ?, '{}', 0, ?)",
                        id, content, TenantContextHolder.getRequiredTenantId());
            }
        } catch (Exception e) {
            // 通知故障不能让已经保存成功的反馈显示为提交失败。
            log.error("反馈 {} 站内信发送失败", feedback.getId(), e);
        }
    }

    public List<MiniNotice> getMiniNotices() {
        if (!security.hasAnyRoles("super_admin", "tenant_admin")) return Collections.emptyList();
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) return Collections.emptyList();
        List<NotifyMessageDO> messages = notifyMessageMapper.selectList(new QueryWrapperX<NotifyMessageDO>()
                .eq("user_id", userId)
                .eq("user_type", 2)
                .eq("template_code", "FEEDBACK_NEW")
                .eq("tenant_id", TenantContextHolder.getRequiredTenantId())
                .orderByDesc("id")
                .last("LIMIT 50"));
        return messages.stream().map(message -> {
            MiniNotice notice = new MiniNotice();
            notice.setId(message.getId());
            notice.setTitle("收到新的帮助与反馈");
            notice.setContent(message.getTemplateContent());
            notice.setType(4);
            notice.setPushTime(message.getCreateTime());
            notice.setReadStatus(message.getReadStatus());
            return notice;
        }).collect(Collectors.toList());
    }

    private Config readConfig() {
        Long tenantId = TenantContextHolder.getRequiredTenantId();
        List<Config> rows = jdbcTemplate.query(
                "SELECT enabled, recipient_user_ids FROM system_feedback_notify_config WHERE tenant_id = ?",
                (rs, rowNum) -> {
                    Config config = new Config();
                    config.setEnabled(rs.getInt("enabled") == 1);
                    config.setRecipientUserIds(JSON.parseArray(rs.getString("recipient_user_ids"), Long.class));
                    return config;
                }, tenantId);
        if (!rows.isEmpty()) return rows.get(0);
        Config fallback = new Config();
        fallback.setEnabled(true);
        fallback.setRecipientUserIds(new ArrayList<>(getAdminIds()));
        return fallback;
    }

    private void requireAdmin() {
        if (!security.hasAnyRoles("super_admin", "tenant_admin")) {
            throw new AccessDeniedException("仅管理员可配置反馈通知");
        }
    }

    @Data
    public static class Config {
        private Boolean enabled;
        private List<Long> recipientUserIds;
    }

    @Data
    public static class MiniNotice {
        private Long id;
        private String title;
        private String content;
        private Integer type;
        private LocalDateTime pushTime;
        private Boolean readStatus;
    }
}
