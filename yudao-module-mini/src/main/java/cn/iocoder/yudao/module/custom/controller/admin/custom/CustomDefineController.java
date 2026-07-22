package cn.iocoder.yudao.module.custom.controller.admin.custom;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.ratelimiter.core.annotation.RateLimiter;
import cn.iocoder.yudao.framework.ratelimiter.core.keyresolver.impl.ClientIpRateLimiterKeyResolver;
import cn.iocoder.yudao.module.custom.framework.audit.annotation.AuditLog;
import cn.iocoder.yudao.module.custom.framework.audit.annotation.AuditOperationType;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractPageReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractRespVO;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractSaveReqVO;
import cn.iocoder.yudao.module.custom.util.ContractRespIdCardEnricher;
import cn.iocoder.yudao.module.custom.controller.admin.custom.vo.*;
import cn.iocoder.yudao.module.custom.controller.admin.wechat.WechatLoginController;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.wechat.MiniUserDo;
import cn.iocoder.yudao.module.custom.dto.ApiResponse;
import cn.iocoder.yudao.module.custom.dto.MpVO;
import cn.iocoder.yudao.module.custom.service.custom.CustomDefineService;
import cn.iocoder.yudao.module.custom.service.email.EmailCodeService;
import cn.iocoder.yudao.module.pay.api.notify.dto.PayOrderNotifyReqDTO;
import cn.iocoder.yudao.framework.common.util.idcard.IdCardCipherUtil;
import cn.iocoder.yudao.module.system.framework.idcard.IdCardCipherService;
import cn.iocoder.yudao.module.system.framework.idcard.IdCardDecryptAccessLimiter;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper;
import cn.iocoder.yudao.module.pay.controller.admin.demo.vo.order.PayDemoOrderCreateReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderRespVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.module.pay.service.order.PayOrderService;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_ID_CARD_DECRYPT_CIPHER_IS_PLAIN;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_ID_CARD_DECRYPT_FAIL;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_NOT_EXISTS;

@Tag(name = "管理后台 - 自定义客户端")
@RestController
@RequestMapping("/custom/contract/dashboard")
@Validated
@Slf4j
public class CustomDefineController {

    @Resource
    private PayOrderService orderService;
    @Resource
    private CustomDefineService customDefineService;
    @Resource
    private EmailCodeService emailCodeService;
    @Resource
    private AdminUserService userService;
    @Resource
    private AdminUserMapper userMapper;

    @Resource
    private IdCardCipherService idCardCipherService;

    @Resource
    private IdCardDecryptAccessLimiter idCardDecryptAccessLimiter;

    @GetMapping("/staticsContractByTimePeriod")
    @Operation(summary = "按照时间窗口统计数据")
    @PreAuthorize("@ss.hasPermission('custom:contract:statics')")
    public CommonResult<StaticsContractPeriodRespVO> staticsContractByTimePeriod() {
        return success(customDefineService.staticsContractByTimePeriod());
    }
    @GetMapping("/user-dimension")
    @Operation(summary = "统计用户状态下数量")
    @PreAuthorize("@ss.hasPermission('custom:contract:dimension')")
    public CommonResult<DimensionCombineRespVo> userDimension() {
        return success(customDefineService.userDimension());
    }

    @GetMapping("/rencentContractList")
    @Operation(summary = "最近联系")
    @PreAuthorize("@ss.hasPermission('custom:contract:recent')")
    public CommonResult<List<RecentContractVO>> rencentContractList() {
        return success(customDefineService.rencentContractList());
    }

    @GetMapping("/creditSearch")
    @Operation(summary = "信用查询")
    // 权限判断改为CreditQueryAccessChecker内手动检查(需支持"有合同往来即可互查"的OR条件，
    // 单纯@PreAuthorize字符串权限表达不了)，见CustomDefineServiceImpl#creditSearch
    @RateLimiter(time = 60, count = 20,
            keyResolver = cn.iocoder.yudao.framework.ratelimiter.core.keyresolver.impl.UserRateLimiterKeyResolver.class,
            message = "信用查询过于频繁，请稍后再试")
    public CommonResult<Page<CreditSearchVO>> creditSearch(CreditPageReqVO creditPageReqVO) {
        return success(customDefineService.creditSearch(creditPageReqVO));
    }
    @PutMapping("/edit")
    @Operation(summary = "补合约")
    @PreAuthorize("@ss.hasPermission('custom:contract:update')")
    public CommonResult<Integer> edit(@RequestBody ContractSaveReqVO contractSaveReqVO) {
        return success(customDefineService.edit(contractSaveReqVO));
    }

    @GetMapping("/totalInfos")
    @Operation(summary = "合同总数数据")
    @PreAuthorize("@ss.hasPermission('custom:contract:query')")
    public CommonResult<TotalInfosRespVO> totalInfos(@Valid ContractPageReqVO pageReqVO) {
        TotalInfosRespVO totalInfosRespVO = customDefineService.totalInfos(pageReqVO);
        return success(totalInfosRespVO);
    }

    @GetMapping("/page")
    @Operation(summary = "不分页数据")
    @PreAuthorize("@ss.hasPermission('custom:contract:query')")
    public CommonResult<List<ContractRespVO>> page(@Valid ContractPageReqDtoVO pageReqVO) {
        List<ContractDO> list = customDefineService.page(pageReqVO);
        List<ContractRespVO> vos = new ArrayList<>(list.size());
        for (ContractDO contract : list) {
            ContractRespVO vo = BeanUtils.toBean(contract, ContractRespVO.class);
            ContractRespIdCardEnricher.enrich(vo, contract, idCardCipherService);
            vos.add(vo);
        }
        return success(vos);
    }
    @PutMapping("/update-pay-password")
    @Operation(summary = "修改支付密码")
    @PreAuthorize("@ss.hasPermission('custom:contract:query')")
    public CommonResult<Boolean> updatePayPassword(@Valid @RequestBody PayPasswordVO passwordVO) {
        try {
            Boolean valid = customDefineService.updatePayPassword(passwordVO);
            return success(valid);
        } catch (Exception e) {
            log.error("修改支付密码失败", e);
            // 1506：自定义错误码，用于支付密码相关错误
            return error(1506, e.getMessage());
        }
    }
    @PostMapping("/checkUserInfo")
    @Operation(summary = "校验用户是否匹配")
    // 权限判断改为CreditQueryAccessChecker内手动检查，见CustomDefineServiceImpl#checkUserInfo
    public CommonResult<Boolean> checkUserInfo(@Valid @RequestBody UserReqVO userReqVO) {
        Boolean valid = customDefineService.checkUserInfo(userReqVO);
        return success(valid);
    }

    @PostMapping("/getPayOrder")
    @Operation(summary = "获取支付订单")
    @PreAuthorize("@ss.hasPermission('custom:contract:query')")
    public CommonResult<PayOrderVO> getPayOrder(@Valid @RequestBody PayOrderVO payOrderVO) {
        PayOrderVO res = customDefineService.getPayOrder(payOrderVO);
        return success(res);
    }

    @PostMapping("/createOrder")
    @Operation(summary = "创建合同订单")
    public CommonResult<Long> createDemoOrder(@Valid @RequestBody ContractPayOrderCreateReqVO createReqVO) {
        return success(customDefineService.createDemoOrder(getLoginUserId(), createReqVO));
    }

    @PostMapping("/updateContractHadConfirm")
    @Operation(summary = "将合同状态更新为已确认")
    @PreAuthorize("@ss.hasPermission('custom:contract:update')")
    public CommonResult<Boolean> updateContractHadConfirm(@RequestBody PayOrderNotifyReqDTO notifyReqDTO) {
        customDefineService.updateContractConfirmedStatus(notifyReqDTO);
        return success(true);
    }

    @PostMapping("/bindmini2user")
    @Operation(summary = "绑定")
    @PermitAll
    public CommonResult<MiniUserDo> bindmini2user(@RequestBody WechatLoginController.WechatLoginRequest wechatLoginRequest) {
        ApiResponse apiResponse = customDefineService.bindmini2user(wechatLoginRequest);
        if (apiResponse.isSuccess()) {
            MiniUserDo miniUserDo = (MiniUserDo) apiResponse.getData();
            return success(miniUserDo);
        } else {
            return error(500, apiResponse.getMessage());
        }
    }
    @PostMapping("/register")
    @Operation(summary = "注册")
    @PermitAll
    @RateLimiter(time = 60, count = 5, keyResolver = ClientIpRateLimiterKeyResolver.class,
            message = "注册请求过于频繁，请稍后再试")
    @AuditLog(module = "用户管理", type = AuditOperationType.CREATE,
            operation = "用户注册-{{#adminUserDO.username}}")
    public CommonResult<Boolean> register(@RequestBody AdminUserDO adminUserDO) {
        customDefineService.register(adminUserDO);
        return success(true);
    }

    @GetMapping("/register-policy")
    @Operation(summary = "获取注册严格等级策略（进注册页调用，小程序据此动态控制）")
    @PermitAll
    public CommonResult<java.util.Map<String, Object>> registerPolicy() {
        return success(new java.util.HashMap<>());
    }
    @PutMapping("/update")
    @Operation(summary = "有效更新")
    @PreAuthorize("@ss.hasPermission('custom:contract:update')")
    public CommonResult<Boolean> update(@RequestBody ContractSaveReqVO contractSaveReqVO) {
        customDefineService.update(contractSaveReqVO);
        return success(true);
    }
    @PutMapping("/updateStatus")
    @Operation(summary = "更新状态")
    @PreAuthorize("@ss.hasPermission('custom:contract:update')")
    public CommonResult<Boolean> updateStatus(@RequestBody ContractSaveReqVO contractSaveReqVO) {
        customDefineService.updateStatus(contractSaveReqVO);
        return success(true);
    }
    @PutMapping("/debt")
    @Operation(summary = "销账")
    @PreAuthorize("@ss.hasPermission('custom:contract:update')")
    public CommonResult<Boolean> debt(@RequestBody DebtVO debtVO) {
        customDefineService.debt(debtVO);
        return success(true);
    }
    @PutMapping("/extension")
    @Operation(summary = "展期")
    @PreAuthorize("@ss.hasPermission('custom:contract:update')")
    public CommonResult<Boolean> extension(@RequestBody ContractSaveReqVO contractSaveReqVO) {
        customDefineService.extension(contractSaveReqVO);
        return success(true);
    }
    @GetMapping("/get")
    @Operation(summary = "获得支付订单")
    @Parameters({
            @Parameter(name = "id", description = "编号", required = true, example = "1024"),
            @Parameter(name = "sync", description = "是否同步", example = "true")
    })
    @PreAuthorize("@ss.hasPermission('custom:contract:query')")
    public CommonResult<PayOrderRespVO> getOrder(@RequestParam("id") Long id,
                                                 @RequestParam(value = "sync", required = false) Boolean sync) {
        PayOrderDO order = orderService.getOrder(id);
        // sync 仅在等待支付
        if (Boolean.TRUE.equals(sync) && PayOrderStatusEnum.isWaiting(order.getStatus())) {
            orderService.syncOrderQuietly(order.getId());
            // 重新查询，因为同步后，可能会有变化
            order = orderService.getOrder(id);
        }
        return success(BeanUtils.toBean(order, PayOrderRespVO.class));
    }
    @PutMapping("/bindQrcode")
    @Operation(summary = "绑定订单支付码")
    @Parameters({
            @Parameter(name = "contractId", description = "编号", required = true, example = "1024"),
            @Parameter(name = "codeUrl", description = "二维码地址", example = "https://org.zxhtom.store.index")
    })
    @PreAuthorize("@ss.hasPermission('custom:contract:query')")
    public CommonResult<String> bindQrcode(@RequestBody ContractRespVoDto contractRespVoDto) {
        Long contractId = contractRespVoDto.getId();
        String codeUrl = contractRespVoDto.getCodeUrl();
        return success(customDefineService.bingQrcode(contractId,codeUrl));
    }
    @DeleteMapping("/deleteQrcode")
    @Operation(summary = "删除订单支付码")
    @PreAuthorize("@ss.hasPermission('custom:contract:query')")
    public CommonResult<String> deleteQrcode(@RequestBody ContractRespVoDto contractRespVoDto) {
        Long contractId = contractRespVoDto.getId();
        String codeUrl = contractRespVoDto.getCodeUrl();
        return success(customDefineService.deleteQrcode(contractId,codeUrl));
    }

    @DeleteMapping("/delete24HourContract")
    @Operation(summary = "解除合同")
    public CommonResult<String> delete24HourContract() {
        CommonResult<String> success = success(customDefineService.delete24HourContract());
        return success;
    }
    @GetMapping("/getContract")
    @Operation(summary = "获得合同")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('custom:contract:query')")
    public CommonResult<ContractRespVoDto> getContract(@RequestParam("id") Long id) {
        return success(customDefineService.getContract(id));
    }

    @GetMapping("/business-dimension")
    @Operation(summary = "demo")
    @PreAuthorize("@ss.hasPermission('custom:contract:statics')")
    public CommonResult<StaticsContractPeriodRespVO> dimension() {
        return success(customDefineService.staticsContractByTimePeriod());
    }

    @GetMapping("/latest-data")
    @Operation(summary = "demo")
    public CommonResult<Integer> latest() {
        return success(1);
    }

    @GetMapping("/model")
    @Operation(summary = "小程序显示model")
    @PermitAll
    public CommonResult<String> model(@RequestParam(required = false,defaultValue = "dev") String appVersion ) {
        return success(customDefineService.selectModel(appVersion));
    }
    @GetMapping("/price")
    @Operation(summary = "小程序显示model")
    @PermitAll
    public CommonResult<String> price(@RequestParam(required = false,defaultValue = "dev") String appVersion ) {
        return success(customDefineService.price(appVersion));
    }

    @GetMapping("/contactMp")
    @Operation(summary = "是否关注mp")
    public CommonResult<MpVO> contackMp() {
        try {
            return success(customDefineService.contackMp());
        } catch (Exception e) {
            return error(1506, e.getMessage());
        }
    }

    @PostMapping("/send-email-code")
    @Operation(summary = "发送邮箱验证码")
    @Parameter(name = "email", description = "邮箱地址", required = true, example = "user@example.com")
    @PermitAll
    @RateLimiter(time = 60, count = 3, keyResolver = ClientIpRateLimiterKeyResolver.class,
            message = "发送验证码过于频繁，请稍后再试")
    public CommonResult<Boolean> sendEmailCode(@RequestParam("email") String email) {
        try {
            Boolean result = emailCodeService.sendCode(email);
            return success(result);
        } catch (Exception e) {
            log.error("[sendEmailCode][发送验证码失败，邮箱: {}]", email, e);
            return error(500, "发送验证码失败: " + e.getMessage());
        }
    }

    @PostMapping("/get-email-by-username")
    @Operation(summary = "通过登录名查询脱敏邮箱（用于找回密码提示）")
    @PermitAll
    @RateLimiter(time = 60, count = 5, keyResolver = ClientIpRateLimiterKeyResolver.class,
            message = "查询过于频繁，请稍后再试")
    public CommonResult<GetEmailByUsernameRespVO> getEmailByUsername(@Valid @RequestBody GetEmailByUsernameReqVO reqVO) {
        AdminUserDO user = userMapper.selectByUsername(reqVO.getUsername());
        // 无论用户是否存在，均返回相同结构，防止用户枚举
        GetEmailByUsernameRespVO respVO = new GetEmailByUsernameRespVO();
        if (user == null || user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            respVO.setEmail(null);
            return success(respVO);
        }
        // 脱敏：只返回 a***@domain.com 格式，不暴露完整邮箱
        String email = user.getEmail();
        int atIdx = email.indexOf('@');
        String masked = atIdx > 1
                ? email.charAt(0) + "***" + email.substring(atIdx)
                : "***" + email.substring(Math.max(0, atIdx));
        respVO.setEmail(masked);
        return success(respVO);
    }

    @PostMapping("/reset-password-by-idno")
    @Operation(summary = "通过身份证+姓名重置密码（B 方案：未绑邮箱用户可用此方式找回）")
    @PermitAll
    @RateLimiter(time = 300, count = 5, keyResolver = ClientIpRateLimiterKeyResolver.class,
            message = "重置密码请求过于频繁，请稍后再试")
    @AuditLog(module = "账户安全", type = AuditOperationType.UPDATE, operation = "通过身份证重置密码",
              usernameExpression = "#reqVO.realname")
    public CommonResult<Boolean> resetPasswordByIdNo(@Valid @RequestBody ResetPasswordByIdNoReqVO reqVO) {
        customDefineService.resetPasswordByIdNo(reqVO);
        return success(true);
    }

    @PostMapping("/id-card/decrypt")
    @Operation(summary = "身份证密文解密（按用户限流：短时间调用次数 + 周期内不同证件种数，见 yudao.id-card.decrypt-api）")
    @PreAuthorize("@ss.hasPermission('custom:contract:decypt')")
    public CommonResult<IdCardDecryptRespVO> decryptIdCard(@Valid @RequestBody IdCardDecryptReqVO reqVO) {
        Long userId = getLoginUserId();
        String cipher = reqVO.getCipher().trim();
        if (IdCardCipherUtil.looksLikePlainIdCard(cipher)) {
            log.warn("[decryptIdCard][reject_plain_shape][userId={}][scene={}]", userId, reqVO.getScene());
            return error(USER_ID_CARD_DECRYPT_CIPHER_IS_PLAIN);
        }
        log.info("[decryptIdCard][userId={}][scene={}]", userId, reqVO.getScene());
        idCardDecryptAccessLimiter.checkRequestRate(userId);
        int reserved = idCardDecryptAccessLimiter.reserveDistinctCipherSlot(userId, cipher);
        try {
            String plain = idCardCipherService.decryptOrNull(cipher);
            if (plain == null) {
                idCardDecryptAccessLimiter.rollbackNewDistinctSlot(userId, cipher, reserved);
                return error(USER_ID_CARD_DECRYPT_FAIL);
            }
            IdCardDecryptRespVO respVO = new IdCardDecryptRespVO();
            respVO.setPlain(plain);
            log.info("[decryptIdCard][success][userId={}][scene={}][plainLen={}]", userId, reqVO.getScene(), plain.length());
            return success(respVO);
        } catch (RuntimeException e) {
            idCardDecryptAccessLimiter.rollbackNewDistinctSlot(userId, cipher, reserved);
            throw e;
        }
    }

    @PutMapping("/update-password-by-email")
    @Operation(summary = "通过邮箱重置用户密码")
    @PermitAll
    @RateLimiter(time = 300, count = 5, keyResolver = ClientIpRateLimiterKeyResolver.class,
            message = "重置密码请求过于频繁，请稍后再试")
    @AuditLog(module = "账户安全", type = AuditOperationType.UPDATE, operation = "通过邮箱重置密码",
              usernameExpression = "#reqVO.email")
    public CommonResult<Boolean> updatePasswordByEmail(@Valid @RequestBody UserUpdatePasswordByEmailReqVO reqVO) {
        // 1. 验证邮箱验证码
        Boolean verifyResult = emailCodeService.verifyCode(reqVO.getEmail(), reqVO.getCode());
        if (!verifyResult) {
            log.warn("[updatePasswordByEmail][验证码验证失败，邮箱: {}]", reqVO.getEmail());
            return error(400, "验证码错误或已过期，请重新获取");
        }

        // 2. 根据邮箱查询用户
        AdminUserDO user = userMapper.selectByEmail(reqVO.getEmail());
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }

        // 3. 调用原有的更新密码方法
        userService.updateUserPassword(user.getId(), reqVO.getPassword());
        log.info("[updatePasswordByEmail][通过邮箱({})重置密码成功，用户ID: {}]", reqVO.getEmail(), user.getId());
        return success(true);
    }
}

