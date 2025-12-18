package cn.iocoder.yudao.module.custom.controller.admin.wechat.vo;

import lombok.Data;

import java.util.Map;

/**
 * @author zxhtom
 * 11/20/25
 */
@Data
public class TemplateVO {
    private String idNo;
    private String templateId;
    private Map<String,Object> datas;
}
