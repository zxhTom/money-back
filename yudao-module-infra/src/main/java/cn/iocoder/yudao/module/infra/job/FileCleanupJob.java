package cn.iocoder.yudao.module.infra.job;

import cn.iocoder.yudao.module.infra.dal.mysql.file.FileMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 文件记录清理：删除早于保留天数的 infra_file 记录，控制表膨胀。
 * MinIO 物理对象由存储侧策略清理，本任务只负责数据库记录对账。
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
