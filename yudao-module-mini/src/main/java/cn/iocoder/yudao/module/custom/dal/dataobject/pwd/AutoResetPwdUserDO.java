package cn.iocoder.yudao.module.custom.dal.dataobject.pwd;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("custom_auto_reset_pwd_user")
@KeySequence("custom_auto_reset_pwd_user_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoResetPwdUserDO extends BaseDO {

    @TableId
    private Long id;

    private Long userId;

}
