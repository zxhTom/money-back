package cn.iocoder.yudao.module.custom.dal.dataobject.wechat;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * TODO
 *
 * @author zxhtom
 * 10/9/25
 */
@TableName("mini_user")
@KeySequence("mini_user_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiniUserDo extends BaseDO {
    private Long id;
    private String appId;
    private String openId;
    private Long userId;
    private boolean isFinished;
}
