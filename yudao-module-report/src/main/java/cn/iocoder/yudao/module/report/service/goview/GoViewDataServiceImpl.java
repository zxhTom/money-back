package cn.iocoder.yudao.module.report.service.goview;

import cn.iocoder.yudao.module.report.controller.admin.goview.vo.data.GoViewDataRespVO;
import com.google.common.collect.Maps;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;

/**
 * GoView 数据 Service 实现类
 */
@Service
@Validated
public class GoViewDataServiceImpl implements GoViewDataService {

    /** 只允许 SELECT 查询，禁止任何 DDL/DML */
    private static final Pattern SELECT_ONLY = Pattern.compile(
            "^\\s*SELECT\\b", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /** 危险关键词黑名单（注释绕过统一由 stripComments 处理后检测） */
    private static final List<String> BLOCKED_KEYWORDS = Arrays.asList(
            "INSERT", "UPDATE", "DELETE", "DROP", "CREATE", "ALTER",
            "TRUNCATE", "RENAME", "REPLACE", "EXEC", "EXECUTE",
            "INTO OUTFILE", "INTO DUMPFILE", "LOAD_FILE", "LOAD DATA",
            "CALL", "XP_CMDSHELL", "SP_EXECUTESQL"
    );

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public GoViewDataRespVO getDataBySQL(String sql) {
        validateSql(sql);

        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql);

        GoViewDataRespVO respVO = new GoViewDataRespVO();
        SqlRowSetMetaData metaData = sqlRowSet.getMetaData();
        String[] columnNames = metaData.getColumnNames();
        respVO.setDimensions(Arrays.asList(columnNames));
        respVO.setSource(new LinkedList<>());
        while (sqlRowSet.next()) {
            Map<String, Object> data = Maps.newHashMapWithExpectedSize(columnNames.length);
            for (String columnName : columnNames) {
                data.put(columnName, sqlRowSet.getObject(columnName));
            }
            respVO.getSource().add(data);
        }
        return respVO;
    }

    private void validateSql(String sql) {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("SQL 不能为空");
        }
        // 去除注释后检测，防止 /* DROP */ SELECT ... 绕过
        String stripped = stripComments(sql).trim();

        if (!SELECT_ONLY.matcher(stripped).find()) {
            throw new IllegalArgumentException("GoView 数据查询仅允许 SELECT 语句");
        }
        // 多语句禁止（分号终止后仍有内容）
        if (containsMultiStatement(stripped)) {
            throw new IllegalArgumentException("不允许多语句执行");
        }
        String upper = stripped.toUpperCase();
        for (String kw : BLOCKED_KEYWORDS) {
            if (upper.contains(kw)) {
                throw new IllegalArgumentException("SQL 包含禁止操作: " + kw);
            }
        }
    }

    /** 去除 -- 单行注释和 /* 块注释 */
    private String stripComments(String sql) {
        // 块注释
        sql = sql.replaceAll("/\\*[\\s\\S]*?\\*/", " ");
        // 单行注释
        sql = sql.replaceAll("--[^\r\n]*", " ");
        // #注释 (MySQL)
        sql = sql.replaceAll("#[^\r\n]*", " ");
        return sql;
    }

    /** 检测分号后是否还有非空内容（多语句攻击） */
    private boolean containsMultiStatement(String sql) {
        int idx = sql.indexOf(';');
        if (idx < 0) return false;
        String after = sql.substring(idx + 1).trim();
        return !after.isEmpty();
    }

}
