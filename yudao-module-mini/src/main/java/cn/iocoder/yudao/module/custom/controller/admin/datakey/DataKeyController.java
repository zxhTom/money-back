package cn.iocoder.yudao.module.custom.controller.admin.datakey;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.custom.controller.admin.datakey.vo.DataKeyRespVO;
import cn.iocoder.yudao.module.custom.framework.dataencrypt.core.DataKeyService;
import cn.iocoder.yudao.module.custom.framework.dataencrypt.core.annotation.DataEncryptIgnore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.UNAUTHORIZED;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 数据密钥")
@RestController
@RequestMapping("/custom/data-key")
@Validated
@DataEncryptIgnore
public class DataKeyController {

    @Resource
    private DataKeyService dataKeyService;

    @GetMapping("")
    @Operation(summary = "获取数据半密钥（按当前登录会话派生）")
    public CommonResult<DataKeyRespVO> getDataKey(
            @Parameter(description = "密钥版本，缺省为当前版本；宽限期内可取旧版本") @RequestParam(value = "version", required = false) Long version,
            HttpServletRequest request) {
        String token = DataKeyService.obtainBearerToken(request);
        if (token == null) {
            throw exception(UNAUTHORIZED);
        }
        DataKeyService.KeyVersion kv = dataKeyService.current();
        long targetVersion = kv.version;
        String master = kv.master;
        if (version != null && version != kv.version) {
            String oldMaster = dataKeyService.getMaster(version);
            if (oldMaster != null) {
                targetVersion = version;
                master = oldMaster;
            }
        }
        return success(new DataKeyRespVO(targetVersion, dataKeyService.keyPart(targetVersion, master, token)));
    }

    @PostMapping("/rotate")
    @Operation(summary = "手动轮换数据密钥")
    @PreAuthorize("@ss.hasPermission('system:data-key:rotate')")
    public CommonResult<Long> rotate() {
        return success(dataKeyService.rotate());
    }

}
