package cn.iocoder.yudao.module.system.dal.dataobject.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 密码变更记录：用户自助改密 / 管理员重置 等每次变更都记一条。
 * 只存 bcrypt 密文（password_hash），严禁存明文。
 */
@TableName("custom_password_history")
@Data
public class PasswordHistoryDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String username;
    /** bcrypt 密文，禁止明文 */
    private String passwordHash;
    /** SELF_PROFILE=本人改密 / RESET=重置 / RESET_WITH_PAY=重置(含支付密码) */
    private String scene;
    /** 操作人（登录用户）；本人=与 userId 相同，无上下文(定时任务/找回)则为空 */
    private Long operatorId;
    private String sourceIp;

    private LocalDateTime createTime;
}
