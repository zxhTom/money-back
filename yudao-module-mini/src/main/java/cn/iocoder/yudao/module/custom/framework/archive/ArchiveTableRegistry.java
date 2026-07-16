package cn.iocoder.yudao.module.custom.framework.archive;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Arrays;
import java.util.List;

public class ArchiveTableRegistry {

    @Data
    @AllArgsConstructor
    public static class ArchiveTable {
        private String mysqlTable;
        private String chTable;
        private String timeColumn;
        private int retentionDays;
    }

    // 沿用各表现有保留阈值；ch 表名统一加 arc_ 前缀
    public static List<ArchiveTable> tables() {
        return Arrays.asList(
                new ArchiveTable("infra_api_access_log", "arc_infra_api_access_log", "begin_time", 7),
                new ArchiveTable("infra_api_error_log", "arc_infra_api_error_log", "exception_time", 30),
                new ArchiveTable("system_operate_log", "arc_system_operate_log", "create_time", 30),
                new ArchiveTable("system_login_log", "arc_system_login_log", "create_time", 30),
                new ArchiveTable("infra_job_log", "arc_infra_job_log", "begin_time", 7),
                new ArchiveTable("custom_ip_access_log", "arc_custom_ip_access_log", "create_time", 30),
                new ArchiveTable("custom_security_alert", "arc_custom_security_alert", "create_time", 90),
                new ArchiveTable("custom_data_access_log", "arc_custom_data_access_log", "create_time", 30),
                new ArchiveTable("custom_audit_log", "arc_custom_audit_log", "create_time", 90),
                new ArchiveTable("custom_ip_blacklist_log", "arc_custom_ip_blacklist_log", "create_time", 90),
                new ArchiveTable("custom_password_history", "arc_custom_password_history", "create_time", 180)
        );
    }
}
