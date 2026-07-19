package cn.iocoder.yudao.module.custom.framework.clickhouse.core;

import cn.iocoder.yudao.module.custom.framework.clickhouse.config.ClickHouseProperties;
import com.clickhouse.jdbc.ClickHouseDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Service
@Slf4j
public class ClickHouseArchiveServiceImpl implements ClickHouseArchiveService {

    private static final java.util.regex.Pattern IDENT = java.util.regex.Pattern.compile("^[A-Za-z0-9_]+$");

    private final ClickHouseProperties props;
    private volatile DataSource dataSource; // CH 数据源自持有（非 Spring bean），null=降级/未启用

    public ClickHouseArchiveServiceImpl(ClickHouseProperties props) {
        this.props = props;
    }

    @PostConstruct
    public void init() {
        if (!props.isEnabled()) {
            log.info("[ClickHouse] 未启用（yudao.clickhouse.enabled=false）");
            return;
        }
        try {
            Properties p = new Properties();
            if (props.getUsername() != null) {
                p.setProperty("user", props.getUsername());
            }
            if (props.getPassword() != null) {
                p.setProperty("password", props.getPassword());
            }
            // 走 JDK HttpURLConnection，避免 apache httpclient5 依赖
            p.setProperty("http_connection_provider", "HTTP_URL_CONNECTION");
            // 关闭压缩：既避免 LZ4 依赖，也避免 gzip 请求体格式不匹配（Input is not in the .gz format）
            p.setProperty("compress", "0");
            p.setProperty("decompress", "0");
            p.setProperty("connect_timeout", "3000");
            p.setProperty("socket_timeout", "60000");
            ClickHouseDataSource ds = new ClickHouseDataSource(props.getUrl(), p);
            try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
                s.execute("SELECT 1");
            }
            this.dataSource = ds;
            log.info("[ClickHouse] 数据源已启用并连通 url={}", props.getUrl());
        } catch (Throwable e) { // 含依赖缺失/连接失败：一律降级，绝不拖垮主程序启动
            this.dataSource = null;
            log.warn("[ClickHouse] 初始化失败，归档/回收功能降级（不影响主程序启动）url={} 原因={}",
                    props.getUrl(), e.toString());
        }
    }

    private static void validateIdentifier(String s) {
        if (s == null || !IDENT.matcher(s).matches()) {
            throw new IllegalArgumentException("非法标识符: " + s);
        }
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
        validateIdentifier(table);
        for (String col : columns) {
            validateIdentifier(col);
        }
        if (rows.get(0).length != columns.size()) {
            throw new IllegalArgumentException("列数与数据宽度不一致");
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
