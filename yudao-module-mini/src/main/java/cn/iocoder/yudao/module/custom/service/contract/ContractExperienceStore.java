package cn.iocoder.yudao.module.custom.service.contract;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.system.framework.idcard.IdCardCipherService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/** A single private, short-lived contract draft per account. No draft is exposed by URL or contract ID. */
@Component
@Slf4j
public class ContractExperienceStore {
    private static final String PREFIX = "money:contract-experience:v1:draft:";
    private static final int DRAFT_DAYS = 30;
    private static final int ACTIVITY_DAYS = 3650;

    @Resource private StringRedisTemplate redis;
    @Resource private ObjectMapper mapper;
    @Resource private IdCardCipherService idCardCipherService;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Draft {
        private String direction;
        private String indebtedName;
        private String indebtedId;
        private String creditorName;
        private String creditorId;
        private String salary;
        private String startDate;
        private String endDate;
        private String remarks;
        private Integer selectedOption;
        private String updatedAt;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Activity {
        private String eventId;
        private String type;
        private String title;
        private String detail;
        private String occurredAt;
    }

    public Draft getDraft(Long tenantId, Long userId) {
        try {
            String value = redis.opsForValue().get(key(tenantId, userId));
            return value == null ? null : mapper.readValue(value, Draft.class);
        } catch (IOException ex) {
            log.warn("Contract draft could not be decoded for user {}", userId, ex);
            return null;
        }
    }

    public Draft saveDraft(Long tenantId, Long userId, Draft incoming) {
        if (incoming == null || !("from".equals(incoming.direction) || "to".equals(incoming.direction)))
            throw new IllegalArgumentException("合同草稿方向无效");
        Draft draft = new Draft();
        draft.direction = incoming.direction;
        draft.indebtedName = limited(incoming.indebtedName, 40);
        draft.creditorName = limited(incoming.creditorName, 40);
        draft.indebtedId = cipher(incoming.indebtedId);
        draft.creditorId = cipher(incoming.creditorId);
        draft.salary = limited(incoming.salary, 20);
        draft.startDate = limited(incoming.startDate, 10);
        draft.endDate = limited(incoming.endDate, 10);
        draft.remarks = limited(incoming.remarks, 500);
        draft.selectedOption = incoming.selectedOption != null && incoming.selectedOption >= 0
                && incoming.selectedOption <= 5 ? incoming.selectedOption : 0;
        draft.updatedAt = LocalDateTime.now().toString();
        try {
            redis.opsForValue().set(key(tenantId, userId), mapper.writeValueAsString(draft), DRAFT_DAYS, TimeUnit.DAYS);
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            throw new IllegalStateException("合同草稿保存失败", ex);
        }
        return draft;
    }

    public void deleteDraft(Long tenantId, Long userId) {
        redis.delete(key(tenantId, userId));
    }

    /** Best-effort activity summary, never used as legal evidence or to change a contract balance. */
    public void recordActivity(Long tenantId, Long contractId, String type, String title, String detail) {
        if (tenantId == null || contractId == null) return;
        Activity event = new Activity();
        event.eventId = UUID.randomUUID().toString();
        event.type = limited(type, 32);
        event.title = limited(title, 40);
        event.detail = limited(detail, 180);
        event.occurredAt = LocalDateTime.now().toString();
        String key = activityKey(tenantId, contractId);
        try {
            redis.opsForList().leftPush(key, mapper.writeValueAsString(event));
            redis.opsForList().trim(key, 0, 99);
            redis.expire(key, ACTIVITY_DAYS, TimeUnit.DAYS);
        } catch (Exception ex) {
            log.warn("Contract activity summary unavailable for contract {}", contractId, ex);
        }
    }

    public List<Activity> getActivity(Long tenantId, Long contractId) {
        List<String> values = redis.opsForList().range(activityKey(tenantId, contractId), 0, 49);
        if (values == null || values.isEmpty()) return Collections.emptyList();
        List<Activity> result = new ArrayList<>();
        for (String value : values) {
            try { result.add(mapper.readValue(value, Activity.class)); }
            catch (IOException ex) { log.warn("Contract activity entry could not be decoded", ex); }
        }
        return result;
    }

    private String cipher(String value) {
        if (StrUtil.isBlank(value)) return null;
        String plain = idCardCipherService.decryptRequestToPlain(value);
        if (!plain.matches("[0-9]{17}[0-9Xx]")) throw new IllegalArgumentException("证件号码格式无效");
        return idCardCipherService.requestToStoredCipher(plain);
    }

    private String limited(String value, int max) {
        if (value == null) return "";
        String trimmed = value.trim();
        if (trimmed.length() > max) throw new IllegalArgumentException("合同草稿字段过长");
        return trimmed;
    }

    private String key(Long tenantId, Long userId) {
        if (tenantId == null || userId == null) throw new IllegalArgumentException("请先登录");
        return PREFIX + tenantId + ':' + userId;
    }

    private String activityKey(Long tenantId, Long contractId) {
        if (tenantId == null || contractId == null) throw new IllegalArgumentException("合同参数无效");
        return "money:contract-experience:v1:activity:" + tenantId + ':' + contractId;
    }
}
