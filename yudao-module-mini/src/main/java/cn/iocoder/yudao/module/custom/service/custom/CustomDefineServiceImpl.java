package cn.iocoder.yudao.module.custom.service.custom;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractPageReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractSaveReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.custom.vo.*;
import cn.iocoder.yudao.module.custom.controller.admin.wechat.WechatLoginController;
import cn.iocoder.yudao.module.custom.controller.admin.wechat.vo.TemplateVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.wechat.MiniUserDo;
import cn.iocoder.yudao.module.custom.dal.mysql.contract.ContractMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.custom.CustomDefineMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.wechat.MiniUserMapper;
import cn.iocoder.yudao.module.custom.dto.ApiResponse;
import cn.iocoder.yudao.module.custom.dto.Code2SessionResponse;
import cn.iocoder.yudao.module.custom.dto.MpVO;
import cn.iocoder.yudao.module.custom.service.contract.ContractService;
import cn.iocoder.yudao.module.custom.service.wechat.WechatService;
import cn.iocoder.yudao.module.fee.controller.admin.strategy.vo.FeeCalculationResult;
import cn.iocoder.yudao.module.fee.service.strategy.FeeCalculationService;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import cn.iocoder.yudao.module.pay.api.notify.dto.PayOrderNotifyReqDTO;
import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderCreateReqDTO;
import cn.iocoder.yudao.module.pay.dal.dataobject.demo.PayDemoOrderDO;
import cn.iocoder.yudao.module.pay.dal.mysql.demo.PayDemoOrderMapper;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.UserRoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.permission.UserRoleMapper;
import cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper;
import cn.iocoder.yudao.module.system.service.dept.DeptService;
import cn.iocoder.yudao.module.system.service.permission.RoleService;
import cn.iocoder.yudao.module.system.framework.idcard.IdCardCipherService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import com.alibaba.fastjson.JSON;
import com.anji.captcha.util.MD5Util;
import com.anji.captcha.util.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.addTime;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_PASSWORD_FAILED;

@Service
@Slf4j
public class CustomDefineServiceImpl implements CustomDefineService{
    @Autowired
    MiniUserMapper miniUserMapper;
    @Autowired
    WechatService wechatService;
    @Autowired
    AdminUserService userService;
    @Autowired
    UserRoleMapper userRoleMapper;
    @Autowired
    DeptService deptService;
    @Autowired
    RoleService roleService;
    @Autowired
    ContractService contractService;
    @Autowired
    FeeCalculationService feeCalculationService;
    @Autowired
    PayDemoOrderMapper payDemoOrderMapper;
    @Resource
    private PayOrderApi payOrderApi;
    @Autowired
    AdminUserService adminUserService;
    @Autowired
    CustomDefineMapper customDefineMapper;
    @Autowired
    ContractMapper contractMapper;
    @Autowired
    private AdminUserMapper adminUserMapper;
    @Autowired
    private cn.iocoder.yudao.module.custom.service.contract.ContractOwnershipGuard ownershipGuard;
    @Autowired
    private cn.iocoder.yudao.module.custom.service.contract.ContractTeaseGuard teaseGuard;
    @Autowired
    private cn.iocoder.yudao.module.custom.service.invite.InviteCodeService inviteCodeService;
    @Autowired
    private cn.iocoder.yudao.module.custom.service.invite.RegisterRiskControlGuard registerRiskControlGuard;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private IdCardCipherService idCardCipherService;

    @Resource
    private ConfigApi configApi;

    @Autowired
    private CreditQueryAccessChecker creditQueryAccessChecker;

    public static final String CONFIG_KEY_DEFAULT_MODEL = "custom.contract.default-model";

    @Override
    public StaticsContractPeriodRespVO staticsContractByTimePeriod() {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        if (teaseGuard.isTeasing(loginUserId)) {
            return cn.iocoder.yudao.module.custom.service.contract.ContractTeaseFactory.generateStaticsContractPeriod(loginUserId);
        }
        return customDefineMapper.staticsContractByTimePeriod();
    }

    @Override
    public DimensionCombineRespVo userDimension() {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        if (teaseGuard.isTeasing(loginUserId)) {
            return cn.iocoder.yudao.module.custom.service.contract.ContractTeaseFactory.generateUserDimension(loginUserId);
        }
        DimensionCombineRespVo respVo = new DimensionCombineRespVo();
        UserDimensionRespVO userDimensionRespVO = customDefineMapper.userDimension();
        respVo.setData(userDimensionRespVO);
        List<UserDimensionRespVO> userDimensionRespVOS = customDefineMapper.userDimensions();
        List<String> months = new ArrayList<>();
        List<Long> newUsers = new ArrayList<>();
        List<Long> activeUsers = new ArrayList<>();

        for (UserDimensionRespVO data : userDimensionRespVOS) {
            months.add(data.getKeyNames());
            newUsers.add(data.getNewUsers());
            activeUsers.add(data.getActiveUsers());
        }
        // 反转列表，让时间正序显示
        Collections.reverse(months);
        Collections.reverse(newUsers);
        Collections.reverse(activeUsers);
        UserDimensionChartRespVO userDimensionChartRespVO = new UserDimensionChartRespVO();
        userDimensionChartRespVO.setKeyNames(months);
        userDimensionChartRespVO.setNewUsers(newUsers);
        userDimensionChartRespVO.setActiveUsers(activeUsers);
        respVo.setChart(userDimensionChartRespVO);
        return respVo;
    }

    @Override
    public List<RecentContractVO> rencentContractList() {
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (teaseGuard.isTeasing(loginUser.getId())) {
            return cn.iocoder.yudao.module.custom.service.contract.ContractTeaseFactory.generateRecentContractList(loginUser.getId());
        }
        AdminUserDO user = adminUserService.getUser(loginUser.getId());
        List<RecentContractVO> list = customDefineMapper.rencentContractList(user.getIdNo());
        for (RecentContractVO vo : list) {
            if (vo == null || StrUtil.isBlank(vo.getIdNo())) {
                continue;
            }
            String stored = vo.getIdNo();
            vo.setIdNo(idCardCipherService.storedToCipherForResponse(stored));
            vo.setIdNoDisplay(idCardCipherService.idNoDisplayFromStored(stored));
        }
        return list;
    }

    @Override
    public Page<CreditSearchVO> creditSearch(CreditPageReqVO pageReqVO) {
        if (StrUtil.isNotBlank(pageReqVO.getIdNo())) {
            pageReqVO.setIdNo(idCardCipherService.normalizeRequestToCipher(pageReqVO.getIdNo()));
        }
        if (!creditQueryAccessChecker.canQueryCredit(pageReqVO.getIdNo())) {
            throw new AccessDeniedException("无权限查询该用户信用信息");
        }
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        log.info("[creditSearch][信用查询审计] operator={} idNoCipher={}", loginUserId, pageReqVO.getIdNo());
        if (teaseGuard.isTeasing(loginUserId)) {
            return cn.iocoder.yudao.module.custom.service.contract.ContractTeaseFactory.generateCreditSearchPage(loginUserId, pageReqVO);
        }
        Page<CreditPageReqVO> objectPage = new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize());
        Page<CreditSearchVO> pageList = customDefineMapper.creditSearch(objectPage,pageReqVO);
         return pageList;
    }

    @Override
    public Integer edit(ContractSaveReqVO contractSaveReqVO) {
        normalizeContractIdNos(contractSaveReqVO);
        ContractDO contractDO = contractMapper.selectById(contractSaveReqVO.getId());
        ownershipGuard.checkOrThrow(contractDO, SecurityFrameworkUtils.getLoginUserId());
        ContractDO updateObj = BeanUtils.toBean(contractSaveReqVO, ContractDO.class);

        BeanUtil.copyProperties(updateObj, contractDO, true);
        contractMapper.updateById(contractDO);
        return 1;
    }

    @Override
    public Object borrowStatics() {
        return null;
    }

    @Override
    public TotalInfosRespVO totalInfos(ContractPageReqVO reqVO) {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        if (teaseGuard.isTeasing(loginUserId)) {
            return cn.iocoder.yudao.module.custom.service.contract.ContractTeaseFactory.generateTotalInfos(loginUserId, reqVO);
        }
        LambdaQueryWrapperX<ContractDO> contractDOLambdaQueryWrapperX = new LambdaQueryWrapperX<ContractDO>()
                .likeIfPresent(ContractDO::getIndebtedName, reqVO.getIndebtedName())
                .eqIfPresent(ContractDO::getIndebtedId, reqVO.getIndebtedId())
                .likeIfPresent(ContractDO::getCreditorName, reqVO.getCreditorName())
                .eqIfPresent(ContractDO::getCreditorId, reqVO.getCreditorId())
                .eqIfPresent(ContractDO::getDescription, reqVO.getDescription())
                .eqIfPresent(ContractDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ContractDO::getCreateTime, reqVO.getCreateTime())
                .betweenIfPresent(ContractDO::getStartDate, reqVO.getStartDate())
                .betweenIfPresent(ContractDO::getEndDate, reqVO.getEndDate())
                .eqIfPresent(ContractDO::getReturnType, reqVO.getReturnType())
                .eqIfPresent(ContractDO::getReasonType, reqVO.getReasonType())
                .eqIfPresent(ContractDO::getDetailReason, reqVO.getDetailReason())
                .eqIfPresent(ContractDO::getSalary, reqVO.getSalary())
                .eqIfPresent(ContractDO::getTariff, reqVO.getTariff())
                .eqIfPresent(ContractDO::getFile, reqVO.getFile())
                .orderByDesc(ContractDO::getId);
        List<ContractDO> contractDOS = contractMapper.selectList(contractDOLambdaQueryWrapperX);
        TotalInfosRespVO totalInfosRespVO = new TotalInfosRespVO();
        Double sum = contractDOS.stream().mapToDouble(ContractDO::getSalary).sum();
        totalInfosRespVO.setTotalSalary(sum);
        return totalInfosRespVO;
    }

    @Override
    public List<ContractDO> page(ContractPageReqDtoVO reqVO) {
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (teaseGuard.isTeasing(loginUser.getId())) {
            return cn.iocoder.yudao.module.custom.service.contract.ContractTeaseFactory
                    .generateContractList(loginUser.getId(), reqVO, 8);
        }
        AdminUserDO user = adminUserService.getUser(loginUser.getId());
        if (StringUtils.isEmpty(user.getRealname()) || StringUtils.isEmpty(user.getIdNo())) {
            throw new RuntimeException("请完善个人信息");
        }
        if ("borrow".equals(reqVO.getLoanType())) {
            //借入
            reqVO.setIndebtedName(user.getRealname());
            reqVO.setIndebtedId(user.getIdNo());
        } else {
            //借出
            reqVO.setCreditorName(user.getRealname());
            reqVO.setCreditorId(user.getIdNo());
        }
        LambdaQueryWrapperX<ContractDO> contractDOLambdaQueryWrapperX = new LambdaQueryWrapperX<ContractDO>()
                .likeIfPresent(ContractDO::getIndebtedName, reqVO.getIndebtedName())
                .eqIfPresent(ContractDO::getIndebtedId, reqVO.getIndebtedId())
                .likeIfPresent(ContractDO::getCreditorName, reqVO.getCreditorName())
                .eqIfPresent(ContractDO::getCreditorId, reqVO.getCreditorId())
                .eqIfPresent(ContractDO::getDescription, reqVO.getDescription())
                .eqIfPresent(ContractDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ContractDO::getCreateTime, reqVO.getCreateTime())
                .betweenIfPresent(ContractDO::getStartDate, reqVO.getStartDate())
                .betweenIfPresent(ContractDO::getEndDate, reqVO.getEndDate())
                .eqIfPresent(ContractDO::getReturnType, reqVO.getReturnType())
                .eqIfPresent(ContractDO::getReasonType, reqVO.getReasonType())
                .eqIfPresent(ContractDO::getDetailReason, reqVO.getDetailReason())
                .eqIfPresent(ContractDO::getSalary, reqVO.getSalary())
                .eqIfPresent(ContractDO::getTariff, reqVO.getTariff())
                .eqIfPresent(ContractDO::getFile, reqVO.getFile())
                .orderByDesc(ContractDO::getId);
        log.info("SQL: {}", contractDOLambdaQueryWrapperX.getSqlSegment());
        log.info("参数: {}", reqVO.getIndebtedId());
        List<ContractDO> contractDOS = contractMapper.selectList(contractDOLambdaQueryWrapperX);
        return contractDOS;
    }

    @Override
    public Boolean checkUserInfo(UserReqVO userReqVO) {
        if (!creditQueryAccessChecker.canQueryCredit(userReqVO.getIdNo())) {
            throw new AccessDeniedException("无权限查询该用户信用信息");
        }
        List<AdminUserDO> users = adminUserService.getUserListByRealname(userReqVO.getRealname());
        if (CollectionUtil.isEmpty(users)) {
            return false;
        }
        return users.stream().anyMatch(user -> idCardCipherService.sameIdCard(userReqVO.getIdNo(), user.getIdNo()));
    }

    @Override
    public PayOrderVO getPayOrder(PayOrderVO payOrderVO) {
        return customDefineMapper.getPayOrder(payOrderVO);
    }

    @Override
    public Long createDemoOrder(Long userId, ContractPayOrderCreateReqVO createReqVO) {
        // 校验支付密码
        AdminUserDO user = userService.getUser(userId);
        if (!passwordEncoder.matches(createReqVO.getPassword(), user.getPayPassword())) {
            throw exception(USER_PASSWORD_FAILED);
        }
        // 1.1 获得商品
        ContractDO contractDO = contractMapper.selectById(createReqVO.getContractId());
        Assert.notNull(contractDO, "合同({}) 不存在", createReqVO.getContractId());
        String spuName = String.format("%s->%s", contractDO.getCreditorName(),contractDO.getIndebtedName());
        FeeCalculationResult feeCalculationResult = feeCalculationService.calculateFeeFromDB(BigDecimal.valueOf(contractDO.getSalary()));
        Integer price = feeCalculationResult.getFee().intValue();
        // 1.2 插入 demo 订单
        PayDemoOrderDO demoOrder = new PayDemoOrderDO().setUserId(userId)
                .setSpuId(createReqVO.getContractId()).setSpuName(spuName)
                .setPrice(price).setPayStatus(false).setRefundPrice(0);
        payDemoOrderMapper.insert(demoOrder);

        // 2.1 创建支付单
        Long payOrderId = payOrderApi.createOrder(new PayOrderCreateReqDTO()
                .setAppKey("demo").setUserIp(getClientIP()) // 支付应用
                .setUserId(userId).setUserType(UserTypeEnum.ADMIN.getValue()) // 用户信息
                .setMerchantOrderId(demoOrder.getId().toString()) // 业务的订单编号
                .setSubject(spuName).setBody("").setPrice(price) // 价格信息
                .setExpireTime(addTime(Duration.ofHours(2L)))); // 支付的过期时间
        // 2.2 更新支付单到 demo 订单
        payDemoOrderMapper.updateById(new PayDemoOrderDO().setId(demoOrder.getId())
                .setPayOrderId(payOrderId));
        customDefineMapper.insertContractPayOrder(contractDO.getId(), payOrderId);
        return demoOrder.getId();
    }

    @Override
    public Integer updateContractConfirmedStatus(PayOrderNotifyReqDTO notifyReqDTO) {
        log.info(new Date() + "@@" + notifyReqDTO.getPayOrderId());
        Long contractId = customDefineMapper.selectContractByPayOrderId(notifyReqDTO.getPayOrderId());
        ContractDO contractDO = contractMapper.selectById(contractId);
        contractDO.setStatus(2);
        contractMapper.updateById(contractDO);
        return 1;
    }

    @Override
    public String bingQrcode(Long contractId, String codeUrl) {
        customDefineMapper.updatePayRelation(contractId, codeUrl);
        return customDefineMapper.selectLatestCodeUrlBaseContractId(contractId);
    }

    @Override
    public ContractRespVoDto getContract(Long id) {
        ContractDO contract = contractService.getContract(id);
        ContractRespVoDto contractRespVoDto = BeanUtils.toBean(contract, ContractRespVoDto.class);
        contractRespVoDto.setIndebtedId(idCardCipherService.storedToCipherForResponse(contract.getIndebtedId()));
        contractRespVoDto.setCreditorId(idCardCipherService.storedToCipherForResponse(contract.getCreditorId()));
        contractRespVoDto.setIndebtedIdDisplay(idCardCipherService.maskFromStored(contract.getIndebtedId()));
        contractRespVoDto.setCreditorIdDisplay(idCardCipherService.maskFromStored(contract.getCreditorId()));
        String codeUrl =customDefineMapper.selectLatestCodeUrlBaseContractId(id);
        contractRespVoDto.setCodeUrl(codeUrl);
        return contractRespVoDto;
    }

    @Override
    public String deleteQrcode(Long contractId, String codeUrl) {
        customDefineMapper.updatePayRelation(contractId,"");
        return "";
    }

    @Override
    public Integer update(ContractSaveReqVO contractSaveReqVO) {
        normalizeContractIdNos(contractSaveReqVO);
        ownershipGuard.checkOrThrow(contractMapper.selectById(contractSaveReqVO.getId()), SecurityFrameworkUtils.getLoginUserId());
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        AdminUserDO user = userService.getUser(loginUser.getId());
        ContractDO updateObj = BeanUtils.toBean(contractSaveReqVO, ContractDO.class);
        contractMapper.updateById(updateObj);
        ContractDO contractDO = contractMapper.selectById(updateObj.getId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        TemplateVO templateVO = new TemplateVO();
        templateVO.setIdNo(contractDO.getCreditorId());
        templateVO.setTemplateId("yFKV3NQHSKccb4kaTnp5Sxv0VJtinn0GdjoQYb2rM4Y");
        templateVO.setDatas(new HashMap<String, Object>() {
            {
                put("thing1", user.getRealname());
                put("character_string3", contractDO.getId());
                put("time5", contractDO.getEndDate().format(formatter));
                put("time4", LocalDateTime.now().format(formatter));
            }
        });
        try {
            wechatService.send(templateVO);
            templateVO.setIdNo(contractDO.getIndebtedId());
            wechatService.send(templateVO);
            templateVO.setIdNo(contractDO.getCreditorId());
            wechatService.send(templateVO);
        } catch (WxErrorException e) {
            e.printStackTrace();
        }

        return 1;
    }

    @Override
    public Integer debt(DebtVO debtVO) {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        AdminUserDO user = userService.getUser(loginUserId);
        boolean matches = passwordEncoder.matches(debtVO.getPassword(), user.getPayPassword());
        if (!matches) {
            throw exception(USER_PASSWORD_FAILED);
        }
        ContractDO contractDO = contractMapper.selectById(debtVO.getId());
        ownershipGuard.checkOrThrow(contractDO, loginUserId);
        if (contractDO.getInterest() == null) {
            contractDO.setInterest(0D);
        }
        BigDecimal add = BigDecimal.valueOf(contractDO.getSalary()).add(BigDecimal.valueOf(contractDO.getInterest()));
        if (contractDO.getRefund() == null) {
            contractDO.setRefund(0D);
        }
        BigDecimal refund = BigDecimal.valueOf(contractDO.getRefund()).add(BigDecimal.valueOf(debtVO.getSettlementAmount()));
        contractDO.setRefund(refund.doubleValue());
        if (refund.compareTo(add) >= 0) {
           //该状态
            contractDO.setStatus(3);
        }
        contractMapper.updateById(contractDO);

//        AdminUserDO user = userService.getUser(SecurityFrameworkUtils.getLoginUserId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        TemplateVO templateVO = new TemplateVO();
        templateVO.setIdNo(contractDO.getCreditorId());
        templateVO.setTemplateId("yFKV3NQHSKccb4kaTnp5Sw6fmswku7mh6iA0LDI_Ux4");
        templateVO.setDatas(new HashMap<String, Object>() {
            {
                put("thing1", user.getRealname());
                put("character_string3", contractDO.getId());
                put("amount14", String.format("销账价格:",contractDO.getRefund()));
                put("time4", LocalDateTime.now().format(formatter));
            }
        });
        try {
            wechatService.send(templateVO);
            templateVO.setIdNo(contractDO.getIndebtedId());
            wechatService.send(templateVO);
            templateVO.setIdNo(contractDO.getCreditorId());
            wechatService.send(templateVO);
        } catch (WxErrorException e) {
            e.printStackTrace();
        }
        return 1;
    }

    @Override
    public Integer extension(ContractSaveReqVO contractSaveReqVO) {
        normalizeContractIdNos(contractSaveReqVO);
        ContractDO updateObj = BeanUtils.toBean(contractSaveReqVO, ContractDO.class);
        AdminUserDO user = userService.getUser(SecurityFrameworkUtils.getLoginUserId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        TemplateVO templateVO = new TemplateVO();
        ContractDO contractDO = contractMapper.selectById(contractSaveReqVO.getId());
        ownershipGuard.checkOrThrow(contractDO, SecurityFrameworkUtils.getLoginUserId());
        templateVO.setIdNo(contractDO.getCreditorId());
        templateVO.setTemplateId("yFKV3NQHSKccb4kaTnp5Sxv0VJtinn0GdjoQYb2rM4Y");
        templateVO.setDatas(new HashMap<String, Object>() {
            {
                put("thing1", user.getRealname());
                put("character_string3", contractSaveReqVO.getId());
                put("time5", contractDO.getEndDate().format(formatter));
                put("time4", LocalDateTime.now().format(formatter));
            }
        });
        try {
            wechatService.send(templateVO);
            templateVO.setIdNo(contractDO.getIndebtedId());
            wechatService.send(templateVO);
            templateVO.setIdNo(contractDO.getCreditorId());
            wechatService.send(templateVO);
        } catch (WxErrorException e) {
            e.printStackTrace();
        }
        return contractMapper.updateById(updateObj);
    }

    @Override
    public Integer register(AdminUserDO adminUserDO) {
        // 0. 注册频率风控（超阈值直接拒绝本次并拉黑IP）——始终生效，与邀请码开关无关
        String registerIp = getClientIP();
        registerRiskControlGuard.beforeRegister(registerIp);
        // 0.1 邀请码校验：仅当全局开关开启时才强制；关闭则完全不校验、不记录
        cn.iocoder.yudao.module.custom.dal.dataobject.invite.InviteCodeDO inviteCode = null;
        if (inviteCodeService.isRegisterInviteRequired()) {
            if (org.apache.commons.lang3.StringUtils.isBlank(adminUserDO.getInviteCode())) {
                throw exception(cn.iocoder.yudao.module.custom.enums.CustomErrorCodeConstants.INVITE_CODE_REQUIRED);
            }
            inviteCode = inviteCodeService.validate(adminUserDO.getInviteCode());
        }

        AdminUserDO user = BeanUtils.toBean(adminUserDO, AdminUserDO.class);
        if (org.apache.commons.lang3.StringUtils.isBlank(user.getIdNo())
                && org.apache.commons.lang3.StringUtils.isNotBlank(user.getIdCard())) {
            user.setIdNo(user.getIdCard().trim());
        }
        user.setIdCard(null);
        // 明文身份证（转密文前）：middle 等级下支付密码=身份证后6位需要它
        String plainIdNo = org.apache.commons.lang3.StringUtils.trimToNull(user.getIdNo());
        if (org.apache.commons.lang3.StringUtils.isNotBlank(user.getIdNo())) {
            user.setIdNo(idCardCipherService.normalizeRequestToCipher(user.getIdNo()));
        }

        // 先做不依赖最终 username 的校验：手机号、身份证号
        validateRegisterUserUnique(user);

        user.setId(System.currentTimeMillis());
        if (org.apache.commons.lang3.StringUtils.isEmpty(user.getNickname())) {
            user.setNickname(MD5Util.md5(user.getUsername()));
        }
        String resolvedUsername = resolveUsernameForRegister(user);
        user.setUsername(resolvedUsername);
        // TODO: 按注册严格等级校验登录密码 + 解析支付密码（后端权威校验，不完全信任前端）
        // RegisterPolicyService not available, using defaults
        user.setPayPassword(user.getPassword());
//            user.setUserCode(openId.hashCode());
        DeptDO deptDO = deptService.getDeptByName("合同管理部");
        if (deptDO != null) {
            user.setDeptId(deptDO.getId());
        }
        RoleDO roleDO = roleService.getRoleByName("contract");
        if (roleDO != null) {
            UserRoleDO userRoleDO = new UserRoleDO();
            userRoleDO.setUserId(user.getId());
            userRoleDO.setRoleId(roleDO.getId());
            userRoleMapper.insert(userRoleDO);
        }
        userService.insertUserSimply(user);
        // 记录邀请注册（仅当开关开启且校验出有效邀请码时）
        if (inviteCode != null) {
            inviteCodeService.recordRegistration(inviteCode, user, registerIp);
        }
        return 1;
    }

    /**
     * 注册前校验：mobile、idNo 在 deleted = 0 的用户中唯一（realname 允许重名）
     */
    private void validateRegisterUserUnique(AdminUserDO user) {
        // 校验手机号唯一
        if (org.apache.commons.lang3.StringUtils.isNotBlank(user.getMobile())) {
            AdminUserDO mobileUser = adminUserMapper.selectByMobile(user.getMobile());
            if (mobileUser != null) {
                throw exception(cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_MOBILE_EXISTS);
            }
        }
        // 校验身份证号唯一
        if (org.apache.commons.lang3.StringUtils.isNotBlank(user.getIdNo())) {
            AdminUserDO idNoUser = adminUserMapper.selectByIdNo(user.getIdNo());
            if (idNoUser != null) {
                throw exception(cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_ID_NO_EXISTS);
            }
        }
    }

    private String resolveUsernameForRegister(AdminUserDO user) {
        // 允许重名：username 直接取 realname，登录时若重名则要求补充身份证号消歧
        String realname = org.apache.commons.lang3.StringUtils.trimToEmpty(user.getRealname());
        if (org.apache.commons.lang3.StringUtils.isNotBlank(realname)) {
            return realname;
        }
        String idNo = org.apache.commons.lang3.StringUtils.trimToEmpty(user.getIdNo());
        if (org.apache.commons.lang3.StringUtils.isNotBlank(idNo)) {
            return idNo;
        }
        throw exception(cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_USERNAME_EXISTS);
    }

    private static final String DEFAULT_PASSWORD = "123456";

    private void normalizeContractIdNos(ContractSaveReqVO vo) {
        if (vo == null) {
            return;
        }
        if (StrUtil.isNotBlank(vo.getIndebtedId())) {
            vo.setIndebtedId(idCardCipherService.normalizeRequestToCipher(vo.getIndebtedId()));
        }
        if (StrUtil.isNotBlank(vo.getCreditorId())) {
            vo.setCreditorId(idCardCipherService.normalizeRequestToCipher(vo.getCreditorId()));
        }
    }

    /**
     * 注册时 password 与 payPassword 规则：两都有值则各存各的，只有一个有值则另一个取该值，都为空则用默认 123456
     */
    private void normalizePasswordAndPayPassword(AdminUserDO user) {
        String p = user.getPassword();
        String pp = user.getPayPassword();
        boolean hasP = org.apache.commons.lang3.StringUtils.isNotBlank(p);
        boolean hasPp = org.apache.commons.lang3.StringUtils.isNotBlank(pp);
        if (hasP && hasPp) {
            return;
        }
        if (hasP) {
            user.setPayPassword(p);
        } else if (hasPp) {
            user.setPassword(pp);
        } else {
            user.setPassword(DEFAULT_PASSWORD);
            user.setPayPassword(DEFAULT_PASSWORD);
        }
    }

    @Override
    public String selectModel(String appVersion) {
        String version = customDefineMapper.selectModel(appVersion);
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(version)) {
            return version;
        }
        // 没有精确版本匹配时，改为读取系统参数（管理端"参数管理"页可配置），不再硬编码
        String defaultModel;
        try {
            defaultModel = configApi.getConfigValueByKey(CONFIG_KEY_DEFAULT_MODEL);
        } catch (Exception e) {
            defaultModel = null;
        }
        if ("safe".equals(defaultModel) || "offcial".equals(defaultModel)) {
            return defaultModel;
        }
        // 未配置或配置了非法值，最终兜底safe（最保守，避免误配置导致审核风险）
        return "safe";
    }

    @Override
    public String delete24HourContract() {
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        AdminUserDO user = adminUserService.getUser(loginUser.getId());
        if (StrUtil.isBlank(user.getIdNo())) {
            return "0";
        }
        return customDefineMapper.delete24HourContract(user.getIdNo());
    }

    @Override
    public ApiResponse bindmini2user(WechatLoginController.WechatLoginRequest request) {

        Code2SessionResponse sessionResponse = wechatService.code2Session(request.getCode());

        if (!sessionResponse.isSuccess()) {
            return ApiResponse.error("绑定微信失败" + sessionResponse.getErrmsg());
        }
        log.info(JSON.toJSONString(sessionResponse));
        String openid = sessionResponse.getOpenid();
        String unionid = sessionResponse.getUnionid();

        MiniUserDo miniUser = new MiniUserDo();
        miniUser.setId(System.currentTimeMillis());
        miniUser.setAppId(request.getAppId());
        miniUser.setUnionId(unionid);
        miniUser.setOpenId(openid);
        miniUser.setUserId(SecurityFrameworkUtils.getLoginUserId());
        miniUser.setDeleted(false);
        miniUserMapper.insertWithConflictReplace(miniUser);
        return ApiResponse.success(miniUser);
    }

    @Override
    public Boolean updatePayPassword(PayPasswordVO passwordVO) {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        Assert.notNull(loginUserId, "当前未登录，无法修改支付密码");

        // 查询当前用户
        AdminUserDO user = adminUserService.getUser(loginUserId);
        Assert.notNull(user, "用户不存在");

        String oldEncodedPayPassword = user.getPayPassword();
        String oldPayPassword = passwordVO.getOldPayPassword();
        String newPayPassword = passwordVO.getNewPayPassword();

        // 如果已经设置过支付密码，则需要校验旧支付密码
        if (org.apache.commons.lang3.StringUtils.isNotBlank(oldEncodedPayPassword)) {
            // 旧密码必填
            Assert.isTrue(org.apache.commons.lang3.StringUtils.isNotBlank(oldPayPassword),
                    "原支付密码不能为空");
            // 校验旧支付密码是否正确
            boolean matches = passwordEncoder.matches(oldPayPassword, oldEncodedPayPassword);
            Assert.isTrue(matches, "原支付密码不正确");
        }

        // 新密码必填
        Assert.isTrue(org.apache.commons.lang3.StringUtils.isNotBlank(newPayPassword),
                "新支付密码不能为空");

        // 加密新的支付密码并更新
        AdminUserDO updateObj = new AdminUserDO();
        updateObj.setId(loginUserId);
        updateObj.setPayPassword(passwordEncoder.encode(newPayPassword));
        adminUserMapper.updateById(updateObj);
        return true;
    }

    @Override
    public void resetPasswordByIdNo(ResetPasswordByIdNoReqVO reqVO) {
        String plain = idCardCipherService.resolveToPlain(reqVO.getIdNo());
        AdminUserDO user = adminUserMapper.selectByIdNo(plain);
        if (user == null) {
            throw exception(cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_NOT_EXISTS);
        }
        String inputRealname = reqVO.getRealname() != null ? reqVO.getRealname().trim() : "";
        String dbRealname = user.getRealname() != null ? user.getRealname().trim() : "";
        if (!inputRealname.equals(dbRealname)) {
            throw exception(cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_IDNO_REALNAME_MISMATCH);
        }
        userService.updateUserPasswordAndPayPassword(user.getId(), reqVO.getPassword());
        log.info("[resetPasswordByIdNo][通过身份证+姓名重置密码成功，用户ID: {}]", user.getId());
    }

    @Override
    public MpVO contackMp() {
        MpVO mpVO = new MpVO();
        mpVO.setCode(200);
        List<MpVO> list = customDefineMapper.contackMp(SecurityFrameworkUtils.getLoginUserId());
        if (CollectionUtils.isNotEmpty(list)) {
            if (null == list.get(0).getSubscribeStatus() || 1 == list.get(0).getSubscribeStatus()) {

                mpVO.setCode(1500);
                mpVO.setMsg("请您先关注公众号");
            } else if (1 == list.get(0).getSubscribeStatus()) {
                mpVO.setCode(1500);
                mpVO.setMsg("您之前取消过公众号，请您重新关注公众号");
            }
        } else {
            mpVO.setCode(1500);
            mpVO.setMsg("请您先关注公众号");
        }
        return mpVO;
    }

    @Override
    public Integer updateStatus(ContractSaveReqVO contractSaveReqVO) {
        ContractDO contractDO = contractMapper.selectById(contractSaveReqVO.getId());
        contractDO.setStatus(contractSaveReqVO.getStatus());
        contractMapper.updateById(contractDO);
        return 1;
    }

    @Override
    public String price(String appVersion) {
        return "safe";
    }

}
