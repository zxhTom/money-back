package cn.iocoder.yudao.module.custom.controller.admin.archive;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.custom.framework.archive.ArchiveTableRegistry;
import cn.iocoder.yudao.module.custom.framework.clickhouse.core.ClickHouseArchiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 归档查询")
@RestController
@RequestMapping("/custom/security/archive")
public class ArchiveQueryController {

    @Resource
    private ClickHouseArchiveService ch;

    private static final Set<String> ALLOWED = ArchiveTableRegistry.tables().stream()
            .map(ArchiveTableRegistry.ArchiveTable::getChTable).collect(Collectors.toSet());

    @GetMapping("/tables")
    @Operation(summary = "可查询的归档表")
    @PreAuthorize("@ss.hasPermission('custom:security:archive')")
    public CommonResult<List<String>> tables() {
        return success(new ArrayList<>(ALLOWED));
    }

    @GetMapping("/query")
    @Operation(summary = "归档查询（ClickHouse）")
    @PreAuthorize("@ss.hasPermission('custom:security:archive')")
    public CommonResult<Map<String, Object>> query(
            @RequestParam String table,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize) {
        Map<String, Object> res = new LinkedHashMap<>();
        if (!ch.isEnabled()) {
            res.put("list", Collections.emptyList());
            res.put("total", 0);
            res.put("message", "ClickHouse 未配置");
            return success(res);
        }
        if (!ALLOWED.contains(table)) { // 防注入：只允许白名单表名
            res.put("list", Collections.emptyList());
            res.put("total", 0);
            return success(res);
        }
        List<Object> args = new ArrayList<>();
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        if (start != null && !start.isEmpty()) { where.append(" AND log_time >= ?"); args.add(start); }
        if (end != null && !end.isEmpty()) { where.append(" AND log_time <= ?"); args.add(end); }
        Long total = ((Number) ch.query("SELECT count() c FROM " + table + where, args.toArray())
                .get(0).get("c")).longValue();
        List<Object> qArgs = new ArrayList<>(args);
        qArgs.add(pageSize);
        qArgs.add((pageNo - 1) * pageSize);
        List<Map<String, Object>> list = ch.query(
                "SELECT * FROM " + table + where + " ORDER BY log_time DESC LIMIT ? OFFSET ?", qArgs.toArray());
        res.put("list", list);
        res.put("total", total);
        return success(res);
    }
}
