package cn.iocoder.yudao.module.custom.dal.dataobject.security;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 风控自动改密操作记录（仅追加）。
 * 明文记录新密码，方便管理员在此查看被处置账号的新密码。
 */
@TableName("custom_security_pwd_reset_log")
@Data
public class SecurityPwdResetLogDO {

    private Long id;
    private Long userId;
    private String username;
    /** 新密码明文（风控生成，供管理员查看） */
    private String newPassword;
    private String reason;
    private String alertType;
    private String sourceIp;
    private LocalDateTime createTime;

}
