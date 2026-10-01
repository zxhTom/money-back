package cn.iocoder.yudao.module.system.controller.admin.risk;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.system.service.risk.RiskLockService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class RiskLockControllerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private RiskLockController riskLockController;

    @Mock
    private RiskLockService riskLockService;

    private MockedStatic<SecurityFrameworkUtils> securityFrameworkUtilsMock;

    @BeforeEach
    public void setUp() {
        securityFrameworkUtilsMock = mockStatic(SecurityFrameworkUtils.class);
    }

    @AfterEach
    public void tearDown() {
        securityFrameworkUtilsMock.close();
    }

    @Test
    public void testUnlockUser() {
        Long adminUserId = 1L;
        Long targetUserId = 100L;

        securityFrameworkUtilsMock.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(adminUserId);

        CommonResult<Boolean> result = riskLockController.unlockUser(targetUserId);

        assertTrue(result.isSuccess());
        assertTrue(result.getData());
        verify(riskLockService).unlockUser(adminUserId, targetUserId);
    }
}
