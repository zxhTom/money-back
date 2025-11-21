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
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractSaveReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.custom.vo.*;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.custom.dal.mysql.contract.ContractMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.custom.CustomDefineMapper;
import cn.iocoder.yudao.module.fee.controller.admin.strategy.vo.FeeCalculationResult;
import cn.iocoder.yudao.module.fee.service.strategy.FeeCalculationService;
import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderCreateReqDTO;
import cn.iocoder.yudao.module.pay.dal.dataobject.demo.PayDemoOrderDO;
import cn.iocoder.yudao.module.pay.dal.mysql.demo.PayDemoOrderMapper;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.addTime;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;

@Service
public class CustomDefineServiceImpl implements CustomDefineService{
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
        Page<Object> objectPage = new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize());
        Page<CreditSearchVO> pageList = customDefineMapper.creditSearch(objectPage);
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
        return demoOrder.getId();
    }

}
