package cn.iocoder.yudao.module.custom.controller.admin.custom;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractPageReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractRespVO;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractSaveReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.custom.vo.*;
import cn.iocoder.yudao.module.custom.controller.admin.wechat.WechatLoginController;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.wechat.MiniUserDo;
import cn.iocoder.yudao.module.custom.dto.ApiResponse;
import cn.iocoder.yudao.module.custom.dto.MpVO;
import cn.iocoder.yudao.module.custom.service.custom.CustomDefineService;
import cn.iocoder.yudao.module.custom.service.email.EmailCodeService;
import cn.iocoder.yudao.module.pay.api.notify.dto.PayOrderNotifyReqDTO;
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
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
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
    @Operation(summary = "借用统计")
    @PreAuthorize("@ss.hasPermission('custom:contract:credit')")
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
    public CommonResult<List<ContractDO>> page(@Valid ContractPageReqDtoVO pageReqVO) {
        List<ContractDO> respVO = customDefineService.page(pageReqVO);
        return success(respVO);
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
    @PreAuthorize("@ss.hasPermission('custom:contract:query')")
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
    @PermitAll
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
    public CommonResult<Boolean> register(@RequestBody AdminUserDO adminUserDO) {
        customDefineService.register(adminUserDO);
        return success(true);
    }
    @PutMapping("/update")
    @Operation(summary = "有效更新")
//    @PermitAll
    public CommonResult<Boolean> update(@RequestBody ContractSaveReqVO contractSaveReqVO) {
        customDefineService.update(contractSaveReqVO);
        return success(true);
    }
    @PutMapping("/updateStatus")
    @Operation(summary = "更新状态")
    @PermitAll
    public CommonResult<Boolean> updateStatus(@RequestBody ContractSaveReqVO contractSaveReqVO) {
        customDefineService.updateStatus(contractSaveReqVO);
        return success(true);
    }
    @PutMapping("/debt")
    @Operation(summary = "销账")
    public CommonResult<Boolean> debt(@RequestBody DebtVO debtVO) {
        customDefineService.debt(debtVO);
        return success(true);
    }
    @PutMapping("/extension")
    @Operation(summary = "展期")
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
    @PermitAll // 允许未登录访问，用于找回密码场景
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
    @Operation(summary = "通过登录名查询邮箱")
    @PermitAll // 允许未登录访问，用于找回密码场景
    public CommonResult<GetEmailByUsernameRespVO> getEmailByUsername(@Valid @RequestBody GetEmailByUsernameReqVO reqVO) {
        // 1. 根据登录名查询用户
        AdminUserDO user = userMapper.selectByUsername(reqVO.getUsername());
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }

        // 2. 检查用户是否有邮箱
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            log.warn("[getEmailByUsername][用户({})没有绑定邮箱]", reqVO.getUsername());
            return error(400, "该用户未绑定邮箱，无法通过邮箱找回密码");
        }

        // 3. 返回邮箱（部分脱敏处理）
        GetEmailByUsernameRespVO respVO = new GetEmailByUsernameRespVO();
        respVO.setEmail(user.getEmail());
        log.info("[getEmailByUsername][通过登录名({})查询邮箱成功]", reqVO.getUsername());
        return success(respVO);
    }

    @PutMapping("/update-password-by-email")
    @Operation(summary = "通过邮箱重置用户密码")
    @PermitAll // 允许未登录访问，用于找回密码场景
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

