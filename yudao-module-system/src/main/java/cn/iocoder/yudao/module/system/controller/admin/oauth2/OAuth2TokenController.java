package cn.iocoder.yudao.module.system.controller.admin.oauth2;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.system.controller.admin.oauth2.vo.token.OAuth2AccessTokenPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.oauth2.vo.token.OAuth2AccessTokenRespVO;
import cn.iocoder.yudao.module.system.controller.admin.oauth2.vo.token.UserTokenStatVO;
import cn.iocoder.yudao.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import cn.iocoder.yudao.module.system.dal.mysql.oauth2.OAuth2AccessTokenMapper;
import cn.iocoder.yudao.module.system.enums.logger.LoginLogTypeEnum;
import cn.iocoder.yudao.module.system.service.auth.AdminAuthService;
import cn.iocoder.yudao.module.system.service.oauth2.OAuth2TokenService;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import cn.iocoder.yudao.module.system.framework.security.SensitiveRecordAccess;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;

@Tag(name = "管理后台 - OAuth2.0 令牌")
@RestController
@RequestMapping("/system/oauth2-token")
public class OAuth2TokenController {

    private static final String SESSION_HANDLE_PREFIX = "session:";

    @Resource
    private OAuth2TokenService oauth2TokenService;
    @Resource
    private AdminAuthService authService;
    @Resource
    private OAuth2AccessTokenMapper oauth2AccessTokenMapper;
    @Resource
    private SensitiveRecordAccess recordAccess;
    @Resource
    private SecurityFrameworkService security;
    @Resource
    private PermissionService permissionService;

    @GetMapping("/page")
    @Operation(summary = "获得访问令牌分页", description = "只返回有效期内的")
    @PreAuthorize("@ss.hasPermission('system:oauth2-token:page')")
    public CommonResult<PageResult<OAuth2AccessTokenRespVO>> getAccessTokenPage(@Valid OAuth2AccessTokenPageReqVO reqVO) {
        reqVO.setUserId(recordAccess.userFilter(reqVO.getUserId()));
        if (!recordAccess.administrator()) {
            if (reqVO.getUserType() != null && !Objects.equals(reqVO.getUserType(), UserTypeEnum.ADMIN.getValue())) {
                throw new AccessDeniedException("只能查看本人的后台会话");
            }
            reqVO.setUserType(UserTypeEnum.ADMIN.getValue());
        }
        PageResult<OAuth2AccessTokenDO> pageResult = oauth2TokenService.getAccessTokenPage(reqVO);
        List<OAuth2AccessTokenRespVO> rows = new ArrayList<>();
        for (OAuth2AccessTokenDO token : pageResult.getList()) {
            OAuth2AccessTokenRespVO row = BeanUtils.toBean(token, OAuth2AccessTokenRespVO.class);
            // Old pages pass accessToken to the force-logout API. Return a non-authenticating handle instead.
            row.setAccessToken(token.getId() == null ? null : SESSION_HANDLE_PREFIX + token.getId());
            row.setRefreshToken("已隐藏");
            rows.add(row);
        }
        return success(new PageResult<>(rows, pageResult.getTotal()));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除访问令牌")
    @Parameter(name = "accessToken", description = "访问令牌", required = true, example = "tudou")
    @PreAuthorize("@ss.hasPermission('system:oauth2-token:delete')")
    public CommonResult<Boolean> deleteAccessToken(@RequestParam("accessToken") String accessToken) {
        OAuth2AccessTokenDO token = resolveRevocationTarget(accessToken);
        if (token != null) authService.logout(token.getAccessToken(), LoginLogTypeEnum.LOGOUT_DELETE.getType());
        return success(true);
    }

    @GetMapping("/user-stats")
    @Operation(summary = "按用户统计 Token 数量（Top N）")
    @Parameter(name = "limit", description = "返回条数", example = "10")
    @PreAuthorize("@ss.hasPermission('system:oauth2-token:page')")
    public CommonResult<List<UserTokenStatVO>> getUserTokenStats(
            @RequestParam(defaultValue = "10") int limit) {
        if (limit < 1 || limit > 100) throw exception0(BAD_REQUEST.getCode(), "统计条数必须在1到100之间");
        if (!recordAccess.administrator()) {
            return success(oauth2AccessTokenMapper.selectAdminTokenCountsByUserIds(
                    Collections.singleton(recordAccess.userFilter(null))));
        }
        return success(oauth2AccessTokenMapper.selectTopUsersByTokenCount(limit));
    }

    @GetMapping("/batch-count")
    @Operation(summary = "批量查询指定用户的在用 Token 数")
    @PreAuthorize("@ss.hasPermission('system:user:token-view')")
    public CommonResult<List<UserTokenStatVO>> batchCountByUserIds(
            @RequestParam List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return success(Collections.emptyList());
        }
        if (userIds.size() > 1000 || userIds.contains(null)) throw exception0(BAD_REQUEST.getCode(), "用户编号列表不合法");
        userIds.forEach(recordAccess::userFilter);
        return success(oauth2AccessTokenMapper.selectAdminTokenCountsByUserIds(new LinkedHashSet<>(userIds)));
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除访问令牌")
    @Parameter(name = "accessTokens", description = "访问令牌数组", required = true)
    @PreAuthorize("@ss.hasPermission('system:oauth2-token:delete')")
    public CommonResult<Boolean> deleteAccessTokenList(@RequestParam("accessTokens") List<String> accessTokens) {
        if (accessTokens == null || accessTokens.isEmpty()) return success(true);
        if (accessTokens.size() > 1000) throw exception0(BAD_REQUEST.getCode(), "一次最多处理1000条会话");
        Set<String> tokens = new LinkedHashSet<>();
        // Validate the whole batch before revoking anything, including mixed permitted/forbidden targets.
        for (String handle : accessTokens) {
            OAuth2AccessTokenDO token = resolveRevocationTarget(handle);
            if (token != null) tokens.add(token.getAccessToken());
        }
        tokens.forEach(token -> authService.logout(token, LoginLogTypeEnum.LOGOUT_DELETE.getType()));
        return success(true);
    }

    private OAuth2AccessTokenDO resolveRevocationTarget(String handle) {
        if (handle == null || handle.isEmpty()) throw exception0(BAD_REQUEST.getCode(), "会话标识不能为空");
        OAuth2AccessTokenDO token;
        if (handle.startsWith(SESSION_HANDLE_PREFIX)) {
            try {
                long id = Long.parseLong(handle.substring(SESSION_HANDLE_PREFIX.length()));
                if (id <= 0) throw new NumberFormatException();
                token = oauth2AccessTokenMapper.selectById(id);
            } catch (NumberFormatException e) {
                throw exception0(BAD_REQUEST.getCode(), "会话标识不合法");
            }
        } else {
            // Legacy clients may still hold a raw token; apply the same authorization checks.
            token = oauth2TokenService.getAccessToken(handle);
        }
        if (token == null) return null; // Already offline remains an idempotent success.
        if (!Objects.equals(token.getTenantId(), TenantContextHolder.getRequiredTenantId())) {
            throw new AccessDeniedException("不能操作其他租户的会话");
        }
        recordAccess.userFilter(token.getUserId());
        if (!recordAccess.administrator() && !Objects.equals(token.getUserType(), UserTypeEnum.ADMIN.getValue())) {
            throw new AccessDeniedException("只能操作本人的后台会话");
        }
        if (Objects.equals(token.getUserType(), UserTypeEnum.ADMIN.getValue())
                && !security.hasAnyRoles("super_admin") && permissionService.hasAnyRoles(token.getUserId(), "super_admin")) {
            throw new AccessDeniedException("仅超级管理员可以强制退出超级管理员会话");
        }
        return token;
    }

}
