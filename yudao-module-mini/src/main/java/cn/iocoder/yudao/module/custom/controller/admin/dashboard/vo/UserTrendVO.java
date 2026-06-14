package cn.iocoder.yudao.module.custom.controller.admin.dashboard.vo;

import lombok.Data;

import java.util.List;

@Data
public class UserTrendVO {
    private List<String> dates;
    private List<Long>   newUserCounts;
    private List<Long>   dauCounts;
    private List<Long>   loginHourDist; // 下标 0-23 对应 0~23 点登录次数
}
