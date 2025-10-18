package cn.iocoder.yudao.module.custom.dal.dataobject.contract;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 客户端 DO
 *
 * @author 芋道源码
 */
@TableName("custom_contract")
@KeySequence("custom_contract_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;
    /**
     * 欠款人姓名
     */
    private String indebtedName;
    /**
     * 欠款人身份证
     */
    private String indebtedId;
    /**
     * 被欠款人姓名
     */
    private String creditorName;
    /**
     * 被欠款人身份证
     */
    private String creditorId;
    /**
     * 应用描述
     */
    private String description;
    /**
     * 合同状态
     */
    private Integer status;


}
