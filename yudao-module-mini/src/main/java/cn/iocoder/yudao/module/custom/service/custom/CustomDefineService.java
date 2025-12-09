package cn.iocoder.yudao.module.custom.service.custom;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractPageReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractRespVO;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractSaveReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.custom.vo.*;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.pay.api.notify.dto.PayOrderNotifyReqDTO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

public interface CustomDefineService {
    StaticsContractPeriodRespVO staticsContractByTimePeriod();

    DimensionCombineRespVo userDimension();

    List<RecentContractVO> rencentContractList();

    Page<CreditSearchVO> creditSearch(CreditPageReqVO creditPageReqVO);

    Integer edit(ContractSaveReqVO contractSaveReqVO);

    Object borrowStatics();

    TotalInfosRespVO totalInfos(ContractPageReqVO pageReqVO);

    List<ContractDO> page(ContractPageReqDtoVO pageReqVO);

    Boolean checkUserInfo(UserReqVO userReqVO);

    PayOrderVO getPayOrder(PayOrderVO payOrderVO);

    Long createDemoOrder(Long loginUserId, ContractPayOrderCreateReqVO createReqVO);

    Integer updateContractConfirmedStatus(PayOrderNotifyReqDTO notifyReqDTO);

    String bingQrcode(Long contractId, String codeUrl);


    ContractRespVoDto getContract(Long id);

    String deleteQrcode(Long contractId, String codeUrl);

    Integer update(ContractSaveReqVO contractSaveReqVO);

    Integer debt(DebtVO debtVO);

    Integer extension(ContractSaveReqVO contractSaveReqVO);

    Integer register(AdminUserDO adminUserDO);

    String selectModel();

    String delete24HourContract();
}
