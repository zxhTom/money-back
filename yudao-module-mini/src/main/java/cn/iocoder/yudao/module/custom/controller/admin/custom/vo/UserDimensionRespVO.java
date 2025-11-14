package cn.iocoder.yudao.module.custom.controller.admin.custom.vo;

import lombok.Data;

@Data
public class UserDimensionRespVO {
    private String keyNames;
    private Long activeUsers;
    private Long newUsers;
    private Long vipUsers;
}
