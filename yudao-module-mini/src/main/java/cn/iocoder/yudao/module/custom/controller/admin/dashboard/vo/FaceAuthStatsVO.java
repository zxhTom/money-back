package cn.iocoder.yudao.module.custom.controller.admin.dashboard.vo;

import lombok.Data;

@Data
public class FaceAuthStatsVO {
    private Long unverified; // verified = 0
    private Long verifying;  // verified = 1
    private Long verified;   // verified = 2
}
