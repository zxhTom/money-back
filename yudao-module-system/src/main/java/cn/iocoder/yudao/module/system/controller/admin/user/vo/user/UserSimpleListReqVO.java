package cn.iocoder.yudao.module.system.controller.admin.user.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 用户精简列表 Request VO（POST  body）")
@Data
public class UserSimpleListReqVO {

    @Schema(description = "用户编号列表，不传或空则返回所有启用用户", example = "[1,2,3]")
    private List<Long> ids;
}
