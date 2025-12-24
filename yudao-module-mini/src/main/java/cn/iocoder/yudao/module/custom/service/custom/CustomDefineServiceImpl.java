package cn.iocoder.yudao.module.custom.service.custom;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractPageReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractRespVO;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractSaveReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.custom.vo.*;
import cn.iocoder.yudao.module.custom.controller.admin.wechat.WechatLoginController;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.wechat.MiniUserDo;
import cn.iocoder.yudao.module.custom.dal.mysql.contract.ContractMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.custom.CustomDefineMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.wechat.MiniUserMapper;
import cn.iocoder.yudao.module.custom.dto.ApiResponse;
import cn.iocoder.yudao.module.custom.dto.Code2SessionResponse;
import cn.iocoder.yudao.module.custom.service.contract.ContractService;
import cn.iocoder.yudao.module.custom.service.wechat.WechatService;
import cn.iocoder.yudao.module.fee.controller.admin.strategy.vo.FeeCalculationResult;
import cn.iocoder.yudao.module.fee.service.strategy.FeeCalculationService;
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
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import com.alibaba.fastjson.JSON;
import com.anji.captcha.util.MD5Util;
import com.anji.captcha.util.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.addTime;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;

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
    private PasswordEncoder passwordEncoder;
    @Override
    public StaticsContractPeriodRespVO staticsContractByTimePeriod() {
        return customDefineMapper.staticsContractByTimePeriod();
    }

    @Override
    public DimensionCombineRespVo userDimension() {
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
        AdminUserDO user = adminUserService.getUser(loginUser.getId());
        return customDefineMapper.rencentContractList(user.getIdNo());
    }

    @Override
    public Page<CreditSearchVO> creditSearch(CreditPageReqVO pageReqVO) {
        Page<CreditPageReqVO> objectPage = new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize());
        Page<CreditSearchVO> pageList = customDefineMapper.creditSearch(objectPage,pageReqVO);
         return pageList;
    }

    @Override
    public Integer edit(ContractSaveReqVO contractSaveReqVO) {
        ContractDO contractDO = contractMapper.selectById(contractSaveReqVO.getId());
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
        List<ContractDO> contractDOS = contractMapper.selectList(contractDOLambdaQueryWrapperX);
        return contractDOS;
    }

    @Override
    public Boolean checkUserInfo(UserReqVO userReqVO) {
        List<AdminUserDO> adminUserDO = adminUserService.getUserListByRealname(userReqVO.getRealname());
        if (CollectionUtil.isNotEmpty(adminUserDO)) {
            AdminUserDO dataAdminUserDO = adminUserDO.get(0);
            return userReqVO.getIdNo().equals(dataAdminUserDO.getIdNo());
        }
        return false;
    }

    @Override
    public PayOrderVO getPayOrder(PayOrderVO payOrderVO) {
        return customDefineMapper.getPayOrder(payOrderVO);
    }

    @Override
    public Long createDemoOrder(Long userId, ContractPayOrderCreateReqVO createReqVO ) {
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
        ContractDO updateObj = BeanUtils.toBean(contractSaveReqVO, ContractDO.class);
        contractMapper.updateById(updateObj);
        return 1;
    }

    @Override
    public Integer debt(DebtVO debtVO) {
        ContractDO contractDO = contractMapper.selectById(debtVO.getId());
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
        return 1;
    }

    @Override
    public Integer extension(ContractSaveReqVO contractSaveReqVO) {
        ContractDO updateObj = BeanUtils.toBean(contractSaveReqVO, ContractDO.class);
        return contractMapper.updateById(updateObj);
    }

    @Override
    public Integer register(AdminUserDO adminUserDO) {

        AdminUserDO user = BeanUtils.toBean(adminUserDO, AdminUserDO.class);

        // 新增用户前校验：手机号、身份证号、真实姓名在未删除用户中唯一
        validateRegisterUserUnique(user);

        user.setId(System.currentTimeMillis());
        if (org.apache.commons.lang3.StringUtils.isEmpty(user.getNickname())) {
            user.setNickname(MD5Util.md5(user.getUsername()));
        }
        if (StringUtils.isEmpty(user.getUsername())) {
            user.setUsername(user.getNickname());
        }
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
        return 1;
    }

    /**
     * 注册前校验：mobile、idNo、realname 在 deleted = 0 的用户中唯一
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
        // 校验真实姓名唯一
        if (org.apache.commons.lang3.StringUtils.isNotBlank(user.getRealname())) {
            AdminUserDO realnameUser = adminUserMapper.selectByRealnameEqual(user.getRealname());
            if (realnameUser != null) {
                throw exception(cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_REALNAME_EXISTS);
            }
        }
    }

    @Override
    public String selectModel(String appVersion) {
        if ("dev".equals(appVersion)) {
            return "offcial";
        }
        String version = customDefineMapper.selectModel(appVersion);
        if (org.apache.commons.lang3.StringUtils.isEmpty(version)) {
            return "safe";
        }
        return version;
    }

    @Override
    public String delete24HourContract() {
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        AdminUserDO user = adminUserService.getUser(loginUser.getId());
        return customDefineMapper.delete24HourContract(user.getRealname());
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

}
