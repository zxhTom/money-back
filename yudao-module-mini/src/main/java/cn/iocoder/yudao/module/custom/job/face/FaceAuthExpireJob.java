package cn.iocoder.yudao.module.custom.job.face;

import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 人脸实名认证过期 Job：把"已认证(verified=1) 但超过 N 个月未登录"的用户改回未认证(verified=0)。
 *
 * 用 Spring @Scheduled 实现：应用启动即自动生效，无需在后台"任务管理"里手动激活，也不依赖 infra_job/Quartz 注册。
 * cron 与月数走配置：yudao.face-auth.expire-job-cron（默认每天03:00）、yudao.face-auth.expire-months（默认2）。
 */
@Component
@Slf4j
public class FaceAuthExpireJob {

    @Value("${yudao.face-auth.expire-months:2}")
    private int expireMonths;

    @Resource
    private AdminUserMapper adminUserMapper;

    @Scheduled(cron = "${yudao.face-auth.expire-job-cron:0 0 3 * * ?}")
    public void run() {
        int months = expireMonths > 0 ? expireMonths : 2;
        LocalDateTime threshold = LocalDateTime.now().minusMonths(months);
        // verified=1 且 login_date < 阈值 → verified=0（login_date 为空的不动，避免误伤刚注册未登录的）
        int count = adminUserMapper.update(null, new LambdaUpdateWrapper<AdminUserDO>()
                .eq(AdminUserDO::getVerified, 1)
                .isNotNull(AdminUserDO::getLoginDate)
                .lt(AdminUserDO::getLoginDate, threshold)
                .set(AdminUserDO::getVerified, 0));
        log.info("[FaceAuthExpireJob][超过 {} 个月未登录，已将 {} 个已认证用户改回未认证]", months, count);
    }

}
