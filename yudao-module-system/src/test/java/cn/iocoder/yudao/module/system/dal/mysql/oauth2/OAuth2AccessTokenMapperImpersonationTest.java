package cn.iocoder.yudao.module.system.dal.mysql.oauth2;

import cn.iocoder.yudao.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import org.apache.ibatis.annotations.Select;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static cn.iocoder.yudao.framework.common.biz.system.oauth2.OAuth2TokenCommonApi.ACCOUNT_IMPERSONATION_SCOPE;
import static org.junit.jupiter.api.Assertions.*;

class OAuth2AccessTokenMapperImpersonationTest {

    @Test
    void switchingTokenIsNotAnOrdinaryLogin() {
        OAuth2AccessTokenDO ordinary = new OAuth2AccessTokenDO();
        ordinary.setScopes(Collections.singletonList("read"));
        OAuth2AccessTokenDO legacy = new OAuth2AccessTokenDO();
        OAuth2AccessTokenDO switched = new OAuth2AccessTokenDO();
        switched.setScopes(Arrays.asList("read", ACCOUNT_IMPERSONATION_SCOPE));
        assertTrue(OAuth2AccessTokenMapper.isOrdinaryToken(ordinary));
        assertTrue(OAuth2AccessTokenMapper.isOrdinaryToken(legacy));
        assertFalse(OAuth2AccessTokenMapper.isOrdinaryToken(switched));
    }

    @Test
    void riskQueriesExcludeSwitchingTokens() throws NoSuchMethodException {
        assertExcludesSwitchingToken("selectDistinctUserIdsByIp", String.class);
        assertExcludesSwitchingToken("selectDistinctIpsByUserId", Long.class);
        assertExcludesSwitchingToken("selectTopUsersByTokenCount", int.class);
    }

    private void assertExcludesSwitchingToken(String method, Class<?> parameter) throws NoSuchMethodException {
        Select select = OAuth2AccessTokenMapper.class.getMethod(method, parameter).getAnnotation(Select.class);
        assertNotNull(select);
        String sql = String.join(" ", select.value());
        assertTrue(sql.contains(OAuth2AccessTokenMapper.ORDINARY_TOKEN_CONDITION), method);
    }
}
