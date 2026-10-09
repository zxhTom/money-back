package cn.iocoder.yudao.module.custom.dal.dataobject.milestone;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 系统关键时间节点 DO
 */
@TableName("system_milestone")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 时间点名称（如：小程序年审、域名续费等） */
    private String title;

    /** 分类：MINI_APP=小程序, PAY=支付平台, DOMAIN=域名, SERVER=服务器, WECHAT_WORK=企业微信, OTHER=其他 */
    private String category;

    /** 关键时间节点（到期日/年审截止日） */
    private LocalDate targetDate;

    /** 提前提醒天数（默认30天） */
    private Integer remindDays;

    /** 状态：0=待处理 1=处理中 2=已完成 3=忽略 */
    private Integer status;

    /** 优先级：HIGH=紧急, MEDIUM=普通, LOW=低 */
    private String priority;

    /** 负责人 */
    private String owner;

    /** 费用金额 */
    private BigDecimal cost;

    /** 备注与操作说明路线 */
    private String remark;
}
