package cn.iocoder.yudao.module.custom.dal.dataobject.contract;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 合同 DO
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
    /**
     * 开始时间
     */
    private LocalDateTime startDate;
    /**
     * 结束时间
     */
    private LocalDateTime endDate;
    /**
     * 还款方式
     */
    private String returnType;
    /**
     * 理由
     */
    private String reasonType;
    /**
     * 详细理由
     */
    private String detailReason;
    /**
     * 金额
     */
    private Long salary;
    /**
     * 费率
     */
    private Long tariff;
    /**
     * 借条附件
     */
    private String file;


}