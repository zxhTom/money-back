package cn.iocoder.yudao.module.custom.controller.admin.pwd;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.custom.dal.dataobject.pwd.AutoResetPwdUserDO;
import cn.iocoder.yudao.module.custom.dal.mysql.pwd.AutoResetPwdUserMapper;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_NOT_EXISTS;

@Tag(name = "管理后台 - 定时自动重置密码用户配置")
@RestController
@RequestMapping("/custom/auto-reset-pwd")
@Validated
public class AutoResetPwdUserController {

    @Resource
    private AutoResetPwdUserMapper autoResetPwdUserMapper;
    @Resource
    private AdminUserService adminUserService;

    @GetMapping("/list")
    @Operation(summary = "查询自动重置密码用户列表")
    @PreAuthorize("@ss.hasPermission('system:user:query')")
    public CommonResult<List<AutoResetPwdUserVO>> listUsers() {
        List<AutoResetPwdUserDO> doList = autoResetPwdUserMapper.selectAll();
        List<AutoResetPwdUserVO> result = new ArrayList<>();
        for (AutoResetPwdUserDO item : doList) {
            AdminUserDO user = adminUserService.getUser(item.getUserId());
            AutoResetPwdUserVO vo = new AutoResetPwdUserVO();
            vo.setId(item.getId());
            vo.setUserId(item.getUserId());
            vo.setCreateTime(item.getCreateTime());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setNickname(user.getNickname());
                vo.setEmail(user.getEmail());
                vo.setMobile(user.getMobile());
            }
            result.add(vo);
        }
        return success(result);
    }

    @PostMapping("/add")
    @Operation(summary = "添加用户到自动重置密码集合")
    @PreAuthorize("@ss.hasPermission('system:user:update')")
    public CommonResult<Boolean> addUser(@RequestParam("userId") @NotNull Long userId) {
        AdminUserDO user = adminUserService.getUser(userId);
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }
        if (autoResetPwdUserMapper.selectByUserId(userId) != null) {
            return success(true);
        }
        AutoResetPwdUserDO entity = new AutoResetPwdUserDO();
        entity.setUserId(userId);
        autoResetPwdUserMapper.insert(entity);
        return success(true);
    }

    @DeleteMapping("/remove")
    @Operation(summary = "从自动重置密码集合移除用户")
    @PreAuthorize("@ss.hasPermission('system:user:update')")
    public CommonResult<Boolean> removeUser(@RequestParam("userId") @NotNull Long userId) {
        autoResetPwdUserMapper.deleteByUserId(userId);
        return success(true);
    }

    @Data
    public static class AutoResetPwdUserVO {
        private Long id;
        private Long userId;
        private String username;
        private String nickname;
        private String email;
        private String mobile;
        private java.time.LocalDateTime createTime;
    }

}
