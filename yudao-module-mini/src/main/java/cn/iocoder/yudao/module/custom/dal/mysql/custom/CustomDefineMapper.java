package cn.iocoder.yudao.module.custom.dal.mysql.custom;

import cn.iocoder.yudao.module.custom.controller.admin.custom.vo.*;
import cn.iocoder.yudao.module.custom.dto.MpVO;
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

    Page<CreditSearchVO> creditSearch(@Param("ipage") Page<CreditPageReqVO> ipage, @Param("page") CreditPageReqVO page);

    PayOrderVO getPayOrder(@Param("query") PayOrderVO payOrderVO);

    Integer insertContractPayOrder(@Param("id") Long id, @Param("payOrderId") Long payOrderId);

    Long selectContractByPayOrderId(Long payOrderId);

    Integer updatePayRelation(@Param("contractId") Long contractId, @Param("codeUrl") String codeUrl);

    String selectLatestCodeUrlBaseContractId(@Param("contractId") Long id);

    Integer deleteQrcode(@Param("contractId") Long contractId);

    String selectModel(@Param("appVersion") String appVersion);

    Integer updateVerify(@Param("idCard") String idCard, @Param("verified") int verified);

    String delete24HourContract(@Param("idNo") String idNo);

    String selectOffcialOpenIdByUserId(@Param("id") Long id);

    List<MpVO> contackMp(@Param("userId") Long userId);
}
