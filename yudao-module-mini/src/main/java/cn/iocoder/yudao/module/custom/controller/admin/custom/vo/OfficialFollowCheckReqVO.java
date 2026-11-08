package cn.iocoder.yudao.module.custom.controller.admin.custom.vo;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class OfficialFollowCheckReqVO {
    @NotBlank
    @Pattern(regexp = "[A-Za-z0-9_-]{1,128}")
    private String code;
}
