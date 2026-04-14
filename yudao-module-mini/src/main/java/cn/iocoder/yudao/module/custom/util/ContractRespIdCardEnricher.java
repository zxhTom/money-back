package cn.iocoder.yudao.module.custom.util;

import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractRespVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.system.framework.idcard.IdCardCipherService;

/**
 * 合同 VO：当事人证件密文 + 展示串（与 profile.idNo 同一套加密）
 */
public final class ContractRespIdCardEnricher {

    private ContractRespIdCardEnricher() {
    }

    public static void enrich(ContractRespVO vo, ContractDO contract, IdCardCipherService idCardCipherService) {
        if (vo == null || contract == null || idCardCipherService == null) {
            return;
        }
        vo.setIndebtedId(idCardCipherService.storedToCipherForResponse(contract.getIndebtedId()));
        vo.setCreditorId(idCardCipherService.storedToCipherForResponse(contract.getCreditorId()));
        vo.setIndebtedIdDisplay(idCardCipherService.maskFromStored(contract.getIndebtedId()));
        vo.setCreditorIdDisplay(idCardCipherService.maskFromStored(contract.getCreditorId()));
    }
}
