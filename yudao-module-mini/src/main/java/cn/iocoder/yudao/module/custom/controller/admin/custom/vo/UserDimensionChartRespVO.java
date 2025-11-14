package cn.iocoder.yudao.module.custom.controller.admin.custom.vo;

import lombok.Data;

import java.util.List;

@Data
public class UserDimensionChartRespVO {
    private List<String> keyNames;
    private List<Long> activeUsers;
    private List<Long> newUsers;
    private List<Long> vipUsers;
}
