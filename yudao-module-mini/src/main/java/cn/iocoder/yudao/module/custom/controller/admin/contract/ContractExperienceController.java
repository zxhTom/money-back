package cn.iocoder.yudao.module.custom.controller.admin.contract;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.custom.service.contract.ContractExperienceStore;
import cn.iocoder.yudao.module.custom.service.contract.ContractExperienceStore.Draft;
import cn.iocoder.yudao.module.custom.service.contract.ContractExperienceStore.Activity;
import cn.iocoder.yudao.module.custom.service.contract.ContractOwnershipGuard;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.custom.dal.mysql.contract.ContractMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/** Account-scoped API for the mini program. Deliberately has no anonymous entry point. */
@RestController
@RequestMapping("/custom/contract/experience")
public class ContractExperienceController {
    @Resource private ContractExperienceStore store;
    @Resource private ContractMapper contractMapper;
    @Resource private ContractOwnershipGuard ownershipGuard;

    @GetMapping("/draft")
    @PreAuthorize("@ss.hasPermission('custom:contract:create')")
    @ApiAccessLog(requestEnable = false, responseEnable = false)
    public CommonResult<Draft> getDraft() {
        return success(store.getDraft(tenantId(), userId()));
    }

    @PutMapping("/draft")
    @PreAuthorize("@ss.hasPermission('custom:contract:create')")
    @ApiAccessLog(requestEnable = false, responseEnable = false)
    public CommonResult<Draft> saveDraft(@RequestBody Draft draft) {
        return success(store.saveDraft(tenantId(), userId(), draft));
    }

    @DeleteMapping("/draft")
    @PreAuthorize("@ss.hasPermission('custom:contract:create')")
    @ApiAccessLog(requestEnable = false, responseEnable = false)
    public CommonResult<Boolean> deleteDraft() {
        store.deleteDraft(tenantId(), userId());
        return success(true);
    }

    @GetMapping("/activity")
    @PreAuthorize("@ss.hasPermission('custom:contract:query')")
    @ApiAccessLog(requestEnable = false, responseEnable = false)
    public CommonResult<List<Activity>> getActivity(@RequestParam("contractId") Long contractId) {
        ContractDO contract = contractMapper.selectById(contractId);
        ownershipGuard.checkOrThrow(contract, userId());
        if (!tenantId().equals(contract.getTenantId())) throw new AccessDeniedException("合同不可访问");
        List<Activity> events = new ArrayList<>(store.getActivity(tenantId(), contractId));
        if (contract.getCreateTime() != null) {
            Activity created = new Activity();
            created.setEventId("created:" + contractId);
            created.setType("CREATED");
            created.setTitle("合同创建");
            created.setDetail("合同已提交，后续操作将在此记录");
            created.setOccurredAt(contract.getCreateTime().toString());
            events.add(created);
        }
        return success(events);
    }

    private Long userId() {
        Long id = SecurityFrameworkUtils.getLoginUserId();
        if (id == null) throw new AccessDeniedException("请先登录");
        return id;
    }

    private Long tenantId() {
        Long id = TenantContextHolder.getTenantId();
        if (id == null) throw new AccessDeniedException("租户身份无效");
        return id;
    }
}
