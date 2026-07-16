package cn.iocoder.yudao.module.custom.service.contract;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContractQueryRiskControlGuardTest {

    @Test
    public void testCalculateDelayMs_escalatesThenCaps() {
        assertEquals(300, ContractQueryRiskControlGuard.calculateDelayMs(1));
        assertEquals(600, ContractQueryRiskControlGuard.calculateDelayMs(2));
        assertEquals(3000, ContractQueryRiskControlGuard.calculateDelayMs(10));
        assertEquals(3000, ContractQueryRiskControlGuard.calculateDelayMs(1000));
    }

}
