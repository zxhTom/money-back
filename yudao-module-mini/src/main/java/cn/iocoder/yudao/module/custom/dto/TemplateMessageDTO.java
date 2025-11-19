package cn.iocoder.yudao.module.custom.dto;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class TemplateMessageDTO {

    @NotBlank(message = "接收者openid不能为空")
    private String toUser;

    @NotBlank(message = "模板ID不能为空")
    private String templateId;

    private String url;

    private String miniProgramAppId;

    private String miniProgramPagePath;

    @NotEmpty(message = "模板数据不能为空")
    private List<TemplateData> data;

    @Data
    public static class TemplateData {
        @NotBlank(message = "数据键不能为空")
        private String name;

        @NotBlank(message = "数据值不能为空")
        private String value;

        private String color;
    }
}
