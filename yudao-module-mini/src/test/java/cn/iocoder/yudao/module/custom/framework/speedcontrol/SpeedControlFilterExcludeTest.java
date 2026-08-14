package cn.iocoder.yudao.module.custom.framework.speedcontrol;

import cn.iocoder.yudao.framework.web.config.WebProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 硬排除清单是"限速配错之后还能不能救回来"的唯一保险，
 * 也是支付/微信回调不被打断的唯一保证，所以单独测。
 */
public class SpeedControlFilterExcludeTest {

    private final SpeedControlFilter filter =
            new SpeedControlFilter(new WebProperties(), null, null);

    @Test
    public void testConfigEndpointNeverThrottled() {
        assertTrue(filter.isExcluded("/admin-api/custom/speed-control/get"));
        assertTrue(filter.isExcluded("/admin-api/custom/speed-control/update"));
    }

    @Test
    public void testAuthEndpointsNeverThrottled() {
        assertTrue(filter.isExcluded("/admin-api/system/auth/login"));
        assertTrue(filter.isExcluded("/admin-api/system/auth/logout"));
        assertTrue(filter.isExcluded("/admin-api/system/auth/get-permission-info"));
        assertTrue(filter.isExcluded("/admin-api/system/auth/refresh-token"));
        assertTrue(filter.isExcluded("/app-api/member/auth/login"));
    }

    @Test
    public void testMenuEndpointNeverThrottled() {
        assertTrue(filter.isExcluded("/admin-api/system/menu/list"));
    }

    /** 微信/支付网关不会等几分钟，被限速会直接打断支付与消息回调 */
    @Test
    public void testThirdPartyCallbacksNeverThrottled() {
        assertTrue(filter.isExcluded("/admin-api/pay/notify/order/123"));
        assertTrue(filter.isExcluded("/app-api/pay/notify/refund/abc"));
        assertTrue(filter.isExcluded("/admin-api/offcial/wx123/callback"));
        assertTrue(filter.isExcluded("/app-api/api/mini/callback"));
        assertTrue(filter.isExcluded("/admin-api/infra/file/sec-check-callback"));
    }

    @Test
    public void testActuatorNeverThrottled() {
        assertTrue(filter.isExcluded("/actuator/health"));
    }

    /** 普通业务接口必须仍然受控，否则限速等于没开 */
    @Test
    public void testNormalBusinessEndpointsAreThrottled() {
        assertFalse(filter.isExcluded("/app-api/custom/contract/page"));
        assertFalse(filter.isExcluded("/admin-api/custom/contract/get"));
        assertFalse(filter.isExcluded("/app-api/custom/credit/query"));
        assertFalse(filter.isExcluded("/admin-api/system/user/page"));
        // 名字里带 auth 但不在 auth 路径下的接口不该被误放行
        assertFalse(filter.isExcluded("/app-api/custom/faceAuth/start"));
    }

}
