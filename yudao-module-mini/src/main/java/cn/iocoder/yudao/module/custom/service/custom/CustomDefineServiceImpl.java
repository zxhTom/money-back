package cn.iocoder.yudao.module.custom.service.custom;

import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.custom.controller.admin.custom.vo.*;
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

}
