package cn.iocoder.yudao.module.system.dal.dataobject.user;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.system.enums.common.SexEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

/**
 * 管理后台的用户 DO
 *
 * @author 芋道源码
 */
@TableName(value = "system_users", autoResultMap = true) // 由于 SQL Server 的 system_user 是关键字，所以使用 system_users
@KeySequence("system_users_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDO extends TenantBaseDO {

    /**
     * 用户ID
     */
    @TableId
    private Long id;

    private Integer verified;

    /**
     * 身份证号码
     */
    private String idNo;

    /**
     * 仅接口入参：小程序注册等场景可能与 idNo 同时传，与 idNo 等价（明文或密文）；不落库
     */
    @TableField(exist = false)
    @Schema(description = "身份证（与 idNo 二选一，明文或密文；服务端自动识别）")
    @JsonProperty("idCard")
    private String idCard;

    /**
     * 真实姓名
     */
    private String realname;
    /**
     * 生日
     */
    private Date birthDate;
    /**
     * 居住地址
     */
    private String address;
    /**
     * 职业
     */
    private Integer occupation;
    /**
     * 学历
     */
    private Integer education;

    /**
     * 用户账号
     */
    private String username;
    /**
     * 加密后的密码
     *
     * 因为目前使用 {@link BCryptPasswordEncoder} 加密器，所以无需自己处理 salt 盐
     */
    private String password;
    /**
     * 用户昵称
     */
    private String nickname;
    /**
     * 备注
     */
    private String remark;
    /**
     * 部门 ID
     */
    private Long deptId;
    /**
     * 岗位编号数组
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Set<Long> postIds;
    /**
     * 用户邮箱
     */
    private String email;
    /**
     * 手机号码
     */
    private String mobile;
    /**
     * 用户性别
     *
     * 枚举类 {@link SexEnum}
     */
    private Integer sex;
    /**
     * 用户头像
     */
    private String avatar;
    /**
     * 帐号状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    /**
     * 最后登录IP
     */
    private String loginIp;
    /**
     * 最后登录时间
     */
    private LocalDateTime loginDate;

    /**
     * 支付密码（加密存储），与登录密码区分
     */
    private String payPassword;

    /**
     * 密码强度：0=未知（历史存量），1=弱，2=中，3=强
     * 密码变更时同步计算写入
     */
    private Integer passwordStrength;

    /**
     * 是否禁止用户自主修改密码：0-否，1-是
     * 为 true 时用户调用个人中心改密接口会被拒绝，管理员重置不受限制
     */
    private Boolean disablePwdChange;

}
