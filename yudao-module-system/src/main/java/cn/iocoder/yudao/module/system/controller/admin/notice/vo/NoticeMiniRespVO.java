package cn.iocoder.yudao.module.system.controller.admin.notice.vo;

import lombok.Data;
import java.time.LocalDateTime;

/** 小程序收件箱只返回展示所需字段，不暴露管理端状态和操作信息。 */
@Data
public class NoticeMiniRespVO {
    private Long id;
    private String title;
    private Integer type;
    private String content;
    private LocalDateTime pushTime;
    private LocalDateTime displayStartTime;
    private LocalDateTime displayEndTime;
    private LocalDateTime maintenanceStartTime;
    private LocalDateTime maintenanceEndTime;
}
