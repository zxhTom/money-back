package cn.iocoder.yudao.module.system.controller.admin.user.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PasswordHistoryPageReqVO extends PageParam {

    private Long userId;
    private String username;
    private String scene;
}
