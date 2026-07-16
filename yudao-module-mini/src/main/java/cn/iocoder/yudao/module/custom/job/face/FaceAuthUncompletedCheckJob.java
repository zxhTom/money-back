package cn.iocoder.yudao.module.custom.job.face;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper;
import cn.iocoder.yudao.module.system.service.monitor.SecurityAlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 人脸未认证扫描 Job：扫描近 N 天注册但仍未完成人脸认证（verified≠1）的用户，
 * 汇总成一条 FACE_AUTH_UNCOMPLETED 告警推送给管理员（走 SecurityAlertService 的通知管道）。
 *
 * handler_param：扫描天数 N，默认 7。
 */
@Component
@Slf4j
public class FaceAuthUncompletedCheckJob implements JobHandler {

    private static final int DEFAULT_DAYS = 7;
    private static final int SAMPLE_LIMIT = 20;
    private static final int SEVERITY = 2;

    @Resource
    private AdminUserMapper adminUserMapper;
    @Resource
    private SecurityAlertService securityAlertService;

    @Override
    @TenantIgnore
    public String execute(String param) {
        int days = parseDays(param);
        LocalDateTime since = LocalDateTime.now().minusDays(days);

        List<AdminUserDO> users = adminUserMapper.selectList(
                new LambdaQueryWrapperX<AdminUserDO>()
                        .ge(AdminUserDO::getCreateTime, since)
                        .and(w -> w.isNull(AdminUserDO::getVerified).or().ne(AdminUserDO::getVerified, 1))
                        .orderByDesc(AdminUserDO::getCreateTime));

        if (CollUtil.isEmpty(users)) {
            return String.format("近 %d 天内无未完成人脸认证的用户", days);
        }

        String sample = users.stream().limit(SAMPLE_LIMIT)
                .map(u -> StrUtil.blankToDefault(u.getRealname(), u.getUsername()))
                .collect(Collectors.joining("、"));
        String message = String.format("近 %d 天内有 %d 个用户未完成人脸认证，示例：%s%s",
                days, users.size(), sample, users.size() > SAMPLE_LIMIT ? " 等" : "");

        securityAlertService.saveAsync("FACE_AUTH_UNCOMPLETED", SEVERITY, null, null,
                "/job/face-auth-check", "JOB", null, message);

        log.info("[FaceAuthUncompletedCheck] {}", message);
        return message;
    }

    private int parseDays(String param) {
        if (StrUtil.isBlank(param)) {
            return DEFAULT_DAYS;
        }
        try {
            int d = Integer.parseInt(param.trim());
            return d > 0 ? d : DEFAULT_DAYS;
        } catch (NumberFormatException e) {
            return DEFAULT_DAYS;
        }
    }

}
