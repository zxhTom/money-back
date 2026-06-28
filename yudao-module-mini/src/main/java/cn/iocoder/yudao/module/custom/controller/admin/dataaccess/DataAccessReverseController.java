package cn.iocoder.yudao.module.custom.controller.admin.dataaccess;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.custom.dal.mysql.contract.ContractMapper;
import cn.iocoder.yudao.module.system.controller.admin.user.vo.user.UserPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.DataAccessLogDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.monitor.DataAccessLogMapper;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 数据反查")
@RestController
@RequestMapping("/custom/data-access/reverse")
@Validated
public class DataAccessReverseController {

    @Resource
    private ContractMapper contractMapper;
    @Resource
    private AdminUserService adminUserService;
    @Resource
    private DataAccessLogMapper dataAccessLogMapper;

    @GetMapping("/records")
    @Operation(summary = "查询业务数据记录列表")
    @PreAuthorize("@ss.hasPermission('system:data-access:log:query')")
    public CommonResult<PageResult<Map<String, Object>>> getRecords(
            @RequestParam String module,
            @RequestParam(required = false) String nameKeyword,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(pageNo);
        pageParam.setPageSize(pageSize);

        if ("用户管理".equals(module)) {
            UserPageReqVO reqVO = new UserPageReqVO();
            reqVO.setPageNo(pageNo);
            reqVO.setPageSize(pageSize);
            PageResult<AdminUserDO> page = adminUserService.getUserPage(reqVO);
            List<Map<String, Object>> items = page.getList().stream().map(u -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", u.getId());
                m.put("username", u.getUsername());
                m.put("nickname", u.getNickname());
                m.put("mobile", u.getMobile());
                m.put("status", u.getStatus());
                m.put("createTime", u.getCreateTime());
                return m;
            }).collect(Collectors.toList());
            return success(new PageResult<>(items, page.getTotal()));
        }

        if ("合同管理".equals(module)) {
            PageResult<ContractDO> page = contractMapper.selectPageByNameKeyword(nameKeyword, pageParam);
            List<Map<String, Object>> items = page.getList().stream().map(c -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", c.getId());
                m.put("indebtedName", c.getIndebtedName());
                m.put("creditorName", c.getCreditorName());
                m.put("salary", c.getSalary());
                m.put("status", c.getStatus());
                m.put("createTime", c.getCreateTime());
                return m;
            }).collect(Collectors.toList());
            return success(new PageResult<>(items, page.getTotal()));
        }

        return success(new PageResult<>());
    }

    @GetMapping("/viewers")
    @Operation(summary = "查询哪些人查看过某条业务数据")
    @PreAuthorize("@ss.hasPermission('system:data-access:log:query')")
    public CommonResult<List<DataAccessLogDO>> getViewers(
            @Parameter(description = "模块名") @RequestParam String module,
            @Parameter(description = "实体类型") @RequestParam String entityType,
            @Parameter(description = "记录ID") @RequestParam Long recordId) {
        List<DataAccessLogDO> list = dataAccessLogMapper.selectViewersByRecordId(module, entityType, recordId);
        return success(list);
    }
}
