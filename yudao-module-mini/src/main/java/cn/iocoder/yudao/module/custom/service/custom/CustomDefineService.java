package cn.iocoder.yudao.module.custom.service.custom;

import cn.iocoder.yudao.module.custom.controller.admin.custom.vo.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

public interface CustomDefineService {
    StaticsContractPeriodRespVO staticsContractByTimePeriod();

    DimensionCombineRespVo userDimension();

    List<RecentContractVO> rencentContractList();

    Page<CreditSearchVO> creditSearch(CreditPageReqVO creditPageReqVO);
}
