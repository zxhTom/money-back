package cn.iocoder.yudao.module.system.convert.user;

import cn.iocoder.yudao.module.system.controller.admin.user.vo.profile.UserProfileRespVO;
import cn.iocoder.yudao.module.system.controller.admin.user.vo.user.UserRespVO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserVerificationResponseTest {
    @Test
    void listDetailAndProfileExposeTheSameVerifiedState() {
        checkState(1, 1);
        checkState(0, 0);
    }

    @Test
    void legacyNullStateIsExplicitlyUnverifiedInAdminResponses() {
        checkState(null, 0);
    }

    private void checkState(Integer stored, int expected) {
        AdminUserDO user = new AdminUserDO();
        user.setId(42L);
        user.setVerified(stored);
        UserRespVO detail = UserConvert.INSTANCE.convert(user, null);
        UserRespVO row = UserConvert.INSTANCE.convertList(Collections.singletonList(user),
                Collections.emptyMap()).get(0);
        UserProfileRespVO profile = UserConvert.INSTANCE.convert(user, Collections.emptyList(),
                null, Collections.emptyList());
        ObjectMapper mapper = new ObjectMapper();
        for (Object response : new Object[]{detail, row}) {
            JsonNode json = mapper.valueToTree(response);
            assertEquals(expected, json.path("verified").asInt(-1),
                    "The actual JSON contract must include a numeric verified field");
        }
        // The existing profile endpoint is kept unchanged; numeric states must agree.
        if (stored != null) {
            JsonNode json = mapper.valueToTree(profile);
            assertEquals(expected, json.path("verified").asInt(-1));
        }
    }
}
