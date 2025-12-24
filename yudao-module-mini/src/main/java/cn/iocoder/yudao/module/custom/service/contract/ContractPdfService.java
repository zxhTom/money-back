package cn.iocoder.yudao.module.custom.service.contract;

import java.io.IOException;

/**
 * TODO
 *
 * @author zxhtom
 * 12/23/25
 */
public interface ContractPdfService {

    public byte[] generateLoanAgreementPdf() throws IOException ;
}
