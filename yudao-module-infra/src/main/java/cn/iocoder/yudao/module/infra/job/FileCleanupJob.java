package cn.iocoder.yudao.module.infra.job;

import cn.iocoder.yudao.module.infra.dal.mysql.file.FileMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 临时文件记录清理：仅删除 demo/ 目录下早于保留天数的 infra_file 记录。
 * demo/ 的物理对象由 MinIO 生命周期策略清理；avatar/contract/feedback 等永久目录不清理。
 */
@Component
@Slf4j
public class FileCleanupJob {

    private static final int BATCH = 2000;
    private static final int MAX_ROUNDS = 200;

    @Resource
    private FileMapper fileMapper;

    @Value("${yudao.file-cleanup.enabled:true}")
    private boolean enabled;
    @Value("${yudao.file-cleanup.retention-days:7}")
    private int retentionDays;

    @Scheduled(cron = "${yudao.file-cleanup.cron:0 30 3 * * ?}")
    public void clean() {
        if (!enabled) {
            return;
        }
        LocalDateTime before = LocalDateTime.now().minusDays(Math.max(retentionDays, 1));
        int total = 0;
        for (int i = 0; i < MAX_ROUNDS; i++) {
            int deleted = fileMapper.deleteByCreateTimeBefore(before, BATCH);
            total += deleted;
            if (deleted < BATCH) {
                break;
            }
        }
        if (total > 0) {
            log.info("[FileCleanupJob] 清理文件记录 {} 条（早于 {}，保留 {} 天）", total, before, retentionDays);
        }
    }
}
