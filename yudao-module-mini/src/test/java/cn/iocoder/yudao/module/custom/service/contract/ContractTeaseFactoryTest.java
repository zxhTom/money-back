package cn.iocoder.yudao.module.custom.service.contract;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractPageReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ContractTeaseFactoryTest {

    @Test
    public void testGenerateContract_deterministicForSameUserAndId() {
        ContractDO first = ContractTeaseFactory.generateContract(123L, 1L);
        ContractDO second = ContractTeaseFactory.generateContract(123L, 1L);
        assertEquals(first.getIndebtedName(), second.getIndebtedName());
        assertEquals(first.getCreditorName(), second.getCreditorName());
        assertEquals(first.getIndebtedId(), second.getIndebtedId());
        assertEquals(first.getCreditorId(), second.getCreditorId());
        assertEquals(first.getSalary(), second.getSalary());
    }

    @Test
    public void testGenerateContract_differsAcrossTeasedUsers() {
        ContractDO forUserA = ContractTeaseFactory.generateContract(123L, 1L);
        ContractDO forUserB = ContractTeaseFactory.generateContract(123L, 2L);
        assertNotEquals(forUserA.getIndebtedId(), forUserB.getIndebtedId());
    }

    @Test
    public void testGenerateContract_neverCarriesHoneypotMarker() {
        ContractDO fake = ContractTeaseFactory.generateContract(123L, 1L);
        assertNotEquals(ContractHoneypotFactory.HONEYPOT_CREATOR_MARKER, fake.getCreator());
    }

    @Test
    public void testGenerateContractPage_deterministicForSameParams() {
        ContractPageReqVO req = new ContractPageReqVO();
        req.setPageNo(1);
        req.setPageSize(10);

        PageResult<ContractDO> first = ContractTeaseFactory.generateContractPage(1L, req);
        PageResult<ContractDO> second = ContractTeaseFactory.generateContractPage(1L, req);

        assertEquals(first.getTotal(), second.getTotal());
        assertEquals(first.getList().size(), second.getList().size());
        for (int i = 0; i < first.getList().size(); i++) {
            assertEquals(first.getList().get(i).getIndebtedId(), second.getList().get(i).getIndebtedId());
        }
    }

}
