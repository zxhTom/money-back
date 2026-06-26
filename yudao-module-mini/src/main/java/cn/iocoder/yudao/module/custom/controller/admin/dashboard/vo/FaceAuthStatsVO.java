package cn.iocoder.yudao.module.custom.controller.admin.dashboard.vo;

import lombok.Data;

@Data
public class FaceAuthStatsVO {
    private Long unverified; // verified = 0 或 null
    private Long verified;   // verified = 1
}
