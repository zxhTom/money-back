package cn.iocoder.yudao.module.custom.dal.dataobject.contract;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 合同模型 DO
 * 
 * @author zxhtom
 */
@TableName("contract_model")
@KeySequence("contract_model_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractModelDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 应用版本号
     */
    private String appVersion;

    /**
     * 模式（如：offcial, safe）
     */
    private String model;
}

