package cn.iocoder.yudao.module.custom.service.contract;

import java.io.IOException;

/**
 * @author zxhtom
 * 12/24/25
 */
public interface ConfirmPdfService {

    public byte[] generateAuthorizationConfirmationPdf() throws IOException ;
}
