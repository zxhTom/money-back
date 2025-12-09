package cn.iocoder.yudao.module.custom.dal.mysql.custom;

import cn.iocoder.yudao.module.custom.controller.admin.custom.vo.*;
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

    PayOrderVO getPayOrder(@Param("query") PayOrderVO payOrderVO);

    Integer insertContractPayOrder(@Param("id") Long id, @Param("payOrderId") Long payOrderId);

    Long selectContractByPayOrderId(Long payOrderId);

    Integer updatePayRelation(@Param("contractId") Long contractId, @Param("codeUrl") String codeUrl);

    String selectLatestCodeUrlBaseContractId(@Param("contractId") Long id);

    Integer deleteQrcode(@Param("contractId") Long contractId);

    String selectModel();

    Integer updateVerify(@Param("idCard") String idCard, @Param("verified") int verified);

    String delete24HourContract(@Param("realname") String realname);
}
