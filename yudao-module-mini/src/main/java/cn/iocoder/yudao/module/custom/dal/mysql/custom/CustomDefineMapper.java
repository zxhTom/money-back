package cn.iocoder.yudao.module.custom.dal.mysql.custom;

import cn.iocoder.yudao.module.custom.controller.admin.custom.vo.CreditSearchVO;
import cn.iocoder.yudao.module.custom.controller.admin.custom.vo.RecentContractVO;
import cn.iocoder.yudao.module.custom.controller.admin.custom.vo.StaticsContractPeriodRespVO;
import cn.iocoder.yudao.module.custom.controller.admin.custom.vo.UserDimensionRespVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CustomDefineMapper {
    StaticsContractPeriodRespVO staticsContractByTimePeriod();

    UserDimensionRespVO userDimension();
    List<UserDimensionRespVO> userDimensions();

    List<RecentContractVO> rencentContractList(@Param("idNo") String idNo);

    Page<CreditSearchVO> creditSearch(Page<Object> objectPage);
}
