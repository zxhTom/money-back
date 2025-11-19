package cn.iocoder.yudao.module.custom.service.custom;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
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
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class CustomDefineServiceImpl implements CustomDefineService{
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

}
