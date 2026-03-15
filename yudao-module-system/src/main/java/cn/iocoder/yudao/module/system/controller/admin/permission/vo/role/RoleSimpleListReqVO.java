package cn.iocoder.yudao.module.system.controller.admin.permission.vo.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 角色精简列表 Request VO（POST body）")
@Data
public class RoleSimpleListReqVO {

    @Schema(description = "角色编号列表，不传或空则返回所有启用角色", example = "[1,2,3]")
    private List<Long> ids;
}
