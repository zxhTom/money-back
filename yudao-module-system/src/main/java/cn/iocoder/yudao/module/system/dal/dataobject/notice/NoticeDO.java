package cn.iocoder.yudao.module.system.dal.dataobject.notice;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.system.enums.notice.NoticeTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 通知公告表
 *
 * @author ruoyi
 */
@TableName("system_notice")
@KeySequence("system_notice_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
public class NoticeDO extends TenantBaseDO {

    /**
     * 公告ID
     */
    private Long id;
    /**
     * 公告标题
     */
    private String title;
    /**
     * 公告类型
     *
     * 枚举 {@link NoticeTypeEnum}
     */
    private Integer type;
    /**
     * 公告内容
     */
    private String content;
    /**
     * 公告状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

    /** 最近一次手动推送时间；未推送的公告不进入小程序收件箱。 */
    private LocalDateTime pushTime;

    /** 指定接收人；为空时向当前租户所有用户推送。 */
    private Long targetUserId;

    /** 横幅展示时间；旧公告为空时展示至推送后 24 小时。 */
    private LocalDateTime displayStartTime;
    private LocalDateTime displayEndTime;

    /** 实际计划停机窗口，与横幅展示时间分开。 */
    private LocalDateTime maintenanceStartTime;
    private LocalDateTime maintenanceEndTime;

}
