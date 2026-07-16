package cn.iocoder.yudao.module.custom.controller.admin.recycle;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.custom.framework.clickhouse.core.ClickHouseArchiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 合同回收站")
@RestController
@RequestMapping("/custom/contract/recycle")
public class ContractRecycleController {

    @Resource
    private ClickHouseArchiveService ch;
    @Resource
    private DataSource dataSource;

    @GetMapping("/page")
    @Operation(summary = "回收站分页（ClickHouse）")
    @PreAuthorize("@ss.hasPermission('custom:contract:recycle')")
    public CommonResult<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        Map<String, Object> res = new LinkedHashMap<>();
        if (!ch.isEnabled()) {
            res.put("list", Collections.emptyList());
            res.put("total", 0);
            res.put("message", "ClickHouse 未配置");
            return success(res);
        }
        String where = "";
        List<Object> args = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            where = " WHERE indebted_name LIKE ? OR creditor_name LIKE ? OR indebted_id LIKE ? OR creditor_id LIKE ?";
            String k = "%" + keyword.trim() + "%";
            args.addAll(Arrays.asList(k, k, k, k));
        }
        Long total = ((Number) ch.query("SELECT count() c FROM contract_recycle FINAL" + where,
                args.toArray()).get(0).get("c")).longValue();
        int offset = (pageNo - 1) * pageSize;
        List<Object> qArgs = new ArrayList<>(args);
        qArgs.add(pageSize);
        qArgs.add(offset);
        List<Map<String, Object>> list = ch.query(
                "SELECT * FROM contract_recycle FINAL" + where + " ORDER BY archive_time DESC LIMIT ? OFFSET ?",
                qArgs.toArray());
        res.put("list", list);
        res.put("total", total);
        return success(res);
    }

    @PutMapping("/restore")
    @Operation(summary = "还原到 MySQL")
    @PreAuthorize("@ss.hasPermission('custom:contract:recycle')")
    public CommonResult<Integer> restore(@RequestParam("ids") List<Long> ids) {
        if (!ch.isEnabled() || ids.isEmpty()) {
            return success(0);
        }
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        int restored = 0;
        for (Long id : ids) {
            List<Map<String, Object>> rows = ch.query("SELECT * FROM contract_recycle FINAL WHERE id=?", id);
            if (rows.isEmpty()) {
                continue;
            }
            Integer exists = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM custom_contract WHERE id=?", Integer.class, id);
            if (exists != null && exists > 0) {
                continue; // 原 id 已存在，跳过
            }
            Map<String, Object> r = rows.get(0);
            jdbc.update("INSERT INTO custom_contract(id,indebted_name,indebted_id,creditor_name,creditor_id,"
                            + "description,status,start_date,end_date,return_type,reason_type,create_time,update_time,deleted)"
                            + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,0)",
                    r.get("id"), r.get("indebted_name"), r.get("indebted_id"), r.get("creditor_name"),
                    r.get("creditor_id"), r.get("description"), r.get("status"), r.get("start_date"),
                    r.get("end_date"), r.get("return_type"), r.get("reason_type"),
                    r.get("create_time"), r.get("update_time"));
            ch.execute("DELETE FROM contract_recycle WHERE id=?", id);
            restored++;
        }
        return success(restored);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "回收站彻底删除")
    @PreAuthorize("@ss.hasPermission('custom:contract:recycle')")
    public CommonResult<Boolean> delete(@RequestParam("ids") List<Long> ids) {
        if (ch.isEnabled() && !ids.isEmpty()) {
            String ph = String.join(",", Collections.nCopies(ids.size(), "?"));
            ch.execute("DELETE FROM contract_recycle WHERE id IN (" + ph + ")", ids.toArray());
        }
        return success(true);
    }
}
