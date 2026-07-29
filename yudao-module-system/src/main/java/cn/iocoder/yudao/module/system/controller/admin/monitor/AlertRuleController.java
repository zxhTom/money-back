package cn.iocoder.yudao.module.system.controller.admin.monitor;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.AlertRuleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.AlertRuleNotifyDO;
import cn.iocoder.yudao.module.system.service.monitor.AlertRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 告警规则")
@RestController
@RequestMapping("/custom/security/alert-rule")
@Validated
public class AlertRuleController {

    @Resource
    private AlertRuleService alertRuleService;

    @GetMapping("/list")
    @Operation(summary = "查询所有告警规则")
    @PreAuthorize("@ss.hasPermission('custom:security:alert:query')")
    public CommonResult<List<AlertRuleDO>> list() {
        return success(alertRuleService.listAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询规则详情（含通知人员）")
    @PreAuthorize("@ss.hasPermission('custom:security:alert:query')")
    public CommonResult<Map<String, Object>> detail(@PathVariable Long id) {
        AlertRuleDO rule = alertRuleService.getById(id);
        List<AlertRuleNotifyDO> notifies = alertRuleService.listNotifies(id);
        Map<String, Object> result = new HashMap<>();
        result.put("rule", rule);
        result.put("notifies", notifies);
        return success(result);
    }

    @PostMapping
    @Operation(summary = "新增告警规则")
    @PreAuthorize("@ss.hasPermission('custom:security:alert:handle')")
    public CommonResult<Long> create(@RequestBody AlertRuleReqVO req) {
        return success(alertRuleService.create(req.getRule(), req.getNotifies()));
    }

    @PutMapping
    @Operation(summary = "修改告警规则")
    @PreAuthorize("@ss.hasPermission('custom:security:alert:handle')")
    public CommonResult<Boolean> update(@RequestBody AlertRuleReqVO req) {
        alertRuleService.update(req.getRule(), req.getNotifies());
        return success(true);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除告警规则")
    @PreAuthorize("@ss.hasPermission('custom:security:alert:handle')")
    public CommonResult<Boolean> delete(@PathVariable Long id) {
        alertRuleService.delete(id);
        return success(true);
    }

    @PutMapping("/{id}/enable")
    @Operation(summary = "启用规则")
    @PreAuthorize("@ss.hasPermission('custom:security:alert:handle')")
    public CommonResult<Boolean> enable(@PathVariable Long id) {
        alertRuleService.setEnabled(id, true);
        return success(true);
    }

    @PutMapping("/{id}/disable")
    @Operation(summary = "禁用规则")
    @PreAuthorize("@ss.hasPermission('custom:security:alert:handle')")
    public CommonResult<Boolean> disable(@PathVariable Long id) {
        alertRuleService.setEnabled(id, false);
        return success(true);
    }

    @PutMapping("/{id}/expose-reason")
    @Operation(summary = "设置是否暴露具体拦截原因")
    @PreAuthorize("@ss.hasPermission('custom:security:alert:handle')")
    public CommonResult<Boolean> setExposeReason(@PathVariable Long id, @RequestParam boolean expose) {
        alertRuleService.setExposeReason(id, expose);
        return success(true);
    }

    @Data
    public static class AlertRuleReqVO {
        private AlertRuleDO rule;
        private List<AlertRuleNotifyDO> notifies;
    }
}
