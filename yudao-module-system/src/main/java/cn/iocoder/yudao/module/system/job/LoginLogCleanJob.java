package cn.iocoder.yudao.module.system.job;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.system.service.logger.LoginLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class LoginLogCleanJob implements JobHandler {

    private static final Integer JOB_CLEAN_RETAIN_DAY = 30;
    private static final Integer DELETE_LIMIT = 100;

    @Resource
    private LoginLogService loginLogService;

    @Override
    @TenantIgnore
    public String execute(String param) {
        Integer count = loginLogService.cleanLoginLog(JOB_CLEAN_RETAIN_DAY, DELETE_LIMIT);
        log.info("[execute][定时执行清理登录日志数量 ({}) 个]", count);
        return String.format("定时执行清理登录日志数量 %s 个", count);
    }

}
