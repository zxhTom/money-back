package cn.iocoder.yudao.module.custom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// entity/dto/TemplateMessageSendResult.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateMessageSendResult {
    private boolean success;
    private String msgId;
    private String errorMsg;
    private LocalDateTime sendTime;

    public static TemplateMessageSendResult success(String msgId) {
        return TemplateMessageSendResult.builder()
                .success(true)
                .msgId(msgId)
                .sendTime(LocalDateTime.now())
                .build();
    }

    public static TemplateMessageSendResult failure(String errorMsg) {
        return TemplateMessageSendResult.builder()
                .success(false)
                .errorMsg(errorMsg)
                .sendTime(LocalDateTime.now())
                .build();
    }
}
