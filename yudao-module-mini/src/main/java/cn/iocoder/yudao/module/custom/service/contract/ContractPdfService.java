package cn.iocoder.yudao.module.custom.service.contract;

import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import com.alipay.api.domain.Contract;

import java.io.IOException;

/**
 * TODO
 *
 * @author zxhtom
 * 12/23/25
 */
public interface ContractPdfService {

    public byte[] generateLoanAgreementPdf(ContractDO contractDO) throws IOException ;
}
