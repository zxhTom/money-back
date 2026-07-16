package cn.iocoder.yudao.module.system.controller.admin.monitor;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.IpWhitelistDO;
import cn.iocoder.yudao.module.system.service.monitor.IpWhitelistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IP白名单")
@RestController
@RequestMapping("/custom/security/ip-whitelist")
@Validated
public class IpWhitelistController {

    @Resource
    private IpWhitelistService ipWhitelistService;

    @GetMapping("/page")
    @Operation(summary = "IP白名单分页")
    @PreAuthorize("@ss.hasPermission('custom:security:whitelist:query')")
    public CommonResult<PageResult<IpWhitelistDO>> getPage(@RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                                                           @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        PageParam page = new PageParam();
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        return success(ipWhitelistService.getPage(page));
    }

    @PostMapping("/create")
    @Operation(summary = "新增IP白名单")
    @PreAuthorize("@ss.hasPermission('custom:security:whitelist:update')")
    public CommonResult<Long> create(@RequestBody IpWhitelistReqVO reqVO) {
        return success(ipWhitelistService.create(reqVO.getIp(), reqVO.getRemark()));
    }

    @PutMapping("/update")
    @Operation(summary = "修改IP白名单")
    @PreAuthorize("@ss.hasPermission('custom:security:whitelist:update')")
    public CommonResult<Boolean> update(@RequestBody IpWhitelistReqVO reqVO) {
        ipWhitelistService.update(reqVO.getId(), reqVO.getIp(), reqVO.getRemark(), reqVO.getEnabled());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除IP白名单")
    @PreAuthorize("@ss.hasPermission('custom:security:whitelist:update')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        ipWhitelistService.delete(id);
        return success(true);
    }

    @GetMapping("/internal-allow")
    @Operation(summary = "查询：是否放行内网IP")
    @PreAuthorize("@ss.hasPermission('custom:security:whitelist:query')")
    public CommonResult<Boolean> getInternalAllow() {
        return success(ipWhitelistService.isInternalAllowed());
    }

    @PutMapping("/internal-allow")
    @Operation(summary = "设置：是否放行内网IP")
    @PreAuthorize("@ss.hasPermission('custom:security:whitelist:update')")
    public CommonResult<Boolean> setInternalAllow(@RequestParam("enabled") boolean enabled) {
        ipWhitelistService.setInternalAllow(enabled);
        return success(true);
    }

    @Data
    public static class IpWhitelistReqVO {
        private Long id;
        private String ip;
        private String remark;
        private Integer enabled;
    }
}
