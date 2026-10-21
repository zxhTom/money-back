package cn.iocoder.yudao.module.system.controller.admin.user;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.framework.security.SensitiveRecordAccess;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.user.vo.PasswordHistoryPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.PasswordHistoryDO;
import cn.iocoder.yudao.module.system.dal.mysql.user.PasswordHistoryMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 密码变更记录")
@RestController
@RequestMapping("/system/user/password-history")
@Validated
public class PasswordHistoryController {

    @Resource
    private SensitiveRecordAccess recordAccess;

    @Resource
    private PasswordHistoryMapper passwordHistoryMapper;

    @GetMapping("/page")
    @Operation(summary = "密码变更记录分页（只含密文，不含明文）")
    @PreAuthorize("@ss.hasPermission('custom:security:pwdhistory:query')")
    public CommonResult<PageResult<PasswordHistoryDO>> page(@Validated PasswordHistoryPageReqVO reqVO) {
        Long userId = recordAccess.userFilter(reqVO.getUserId());
        LambdaQueryWrapperX<PasswordHistoryDO> query = new LambdaQueryWrapperX<PasswordHistoryDO>()
                .eqIfPresent(PasswordHistoryDO::getUserId, userId)
                .likeIfPresent(PasswordHistoryDO::getUsername, reqVO.getUsername())
                .eqIfPresent(PasswordHistoryDO::getScene, reqVO.getScene())
                .orderByDesc(PasswordHistoryDO::getId);
        String tenantUsers = recordAccess.tenantUserIdsSql();
        if (tenantUsers != null) query.inSql(PasswordHistoryDO::getUserId, tenantUsers);
        PageResult<PasswordHistoryDO> page = passwordHistoryMapper.selectPage(reqVO, query);
        if (!recordAccess.administrator()) {
            java.util.List<PasswordHistoryDO> safe = new java.util.ArrayList<>();
            for (PasswordHistoryDO row : page.getList()) {
                PasswordHistoryDO copy = BeanUtils.toBean(row, PasswordHistoryDO.class);
                copy.setPasswordHash(null);
                safe.add(copy);
            }
            page = new PageResult<>(safe, page.getTotal());
        }
        return success(page);
    }
}
