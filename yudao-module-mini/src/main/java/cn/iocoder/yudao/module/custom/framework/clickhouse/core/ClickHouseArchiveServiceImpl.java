package cn.iocoder.yudao.module.custom.framework.clickhouse.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Service
@Slf4j
public class ClickHouseArchiveServiceImpl implements ClickHouseArchiveService {

    private final DataSource dataSource; // 可能为 null（未启用）

    public ClickHouseArchiveServiceImpl(
            @Autowired(required = false) @Qualifier("clickHouseDataSource") DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public boolean isEnabled() {
        return dataSource != null;
    }

    private void requireEnabled() {
        if (!isEnabled()) {
            throw new IllegalStateException("ClickHouse 未启用");
        }
    }

    @Override
    public void ensureTable(String ddl) {
        if (!isEnabled()) {
            return;
        }
        execute(ddl);
    }

    @Override
    public int insertRows(String table, List<String> columns, List<Object[]> rows) {
        requireEnabled();
        if (rows.isEmpty()) {
            return 0;
        }
        String cols = String.join(",", columns);
        String ph = String.join(",", Collections.nCopies(columns.size(), "?"));
        String sql = "INSERT INTO " + table + " (" + cols + ") VALUES (" + ph + ")";
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (Object[] row : rows) {
                for (int i = 0; i < row.length; i++) {
                    ps.setObject(i + 1, row[i]);
                }
                ps.addBatch();
            }
            int[] r = ps.executeBatch();
            return r.length;
        } catch (SQLException e) {
            throw new RuntimeException("ClickHouse 写入失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> query(String sql, Object... args) {
        if (!isEnabled()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> out = new ArrayList<>();
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                int n = md.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    for (int i = 1; i <= n; i++) {
                        m.put(md.getColumnLabel(i), rs.getObject(i));
                    }
                    out.add(m);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("ClickHouse 查询失败: " + e.getMessage(), e);
        }
        return out;
    }

    @Override
    public int execute(String sql, Object... args) {
        requireEnabled();
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("ClickHouse 执行失败: " + e.getMessage(), e);
        }
    }
}
