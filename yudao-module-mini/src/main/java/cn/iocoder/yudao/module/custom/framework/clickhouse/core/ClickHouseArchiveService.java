package cn.iocoder.yudao.module.custom.framework.clickhouse.core;

import java.util.List;
import java.util.Map;

public interface ClickHouseArchiveService {
    boolean isEnabled();
    void ensureTable(String ddl);
    int insertRows(String table, List<String> columns, List<Object[]> rows);
    List<Map<String, Object>> query(String sql, Object... args);
    int execute(String sql, Object... args);
}
