package cn.iocoder.yudao.module.custom.service.custom;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.custom.config.WechatConfig;
import cn.iocoder.yudao.module.custom.dal.mysql.custom.CustomDefineMapper;
import cn.iocoder.yudao.module.custom.dto.Code2SessionResponse;
import cn.iocoder.yudao.module.custom.dto.MpVO;
import cn.iocoder.yudao.module.custom.service.wechat.WechatService;
import com.binarywang.spring.starter.wxjava.mp.properties.WxMpProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import javax.annotation.Resource;
import java.util.List;

/** Missing association is unknown, never evidence that a WeChat user unfollowed. */
@Service
public class OfficialAccountFollowService {
    @Resource private CustomDefineMapper mapper;
    @Resource private WechatService wechatService;
    @Resource private WechatConfig miniConfig;
    @Resource private WxMpProperties officialConfig;

    public MpVO check(Long userId, String code) {
        if (userId == null || !StringUtils.hasText(officialConfig.getAppId())) return unknown();
        String unionId = null;
        if (code != null) {
            if (!code.matches("[A-Za-z0-9_-]{1,128}")) return unknown();
            try {
                Code2SessionResponse session = wechatService.code2Session(code);
                if (session == null || !session.isSuccess() || !StringUtils.hasText(session.getOpenid())
                        || !StringUtils.hasText(session.getUnionid())) return unknown();
                unionId = session.getUnionid(); // Only trust the server-to-WeChat exchange.
            } catch (RuntimeException ex) {
                return unknown(); // No binding writes, no false unfollow reminder on provider errors.
            }
        } else if (!StringUtils.hasText(miniConfig.getAppid()) || TenantContextHolder.getTenantId() == null) {
            return unknown();
        }
        List<Integer> statuses = mapper.selectOfficialFollowStates(userId, TenantContextHolder.getTenantId(),
                miniConfig.getAppid(), officialConfig.getAppId(), unionId);
        if (statuses == null || statuses.isEmpty()) return unknown();
        if (statuses.contains(0)) return result(200, "FOLLOWED", "已关注公众号");
        if (statuses.stream().allMatch(value -> Integer.valueOf(1).equals(value))) {
            return result(1500, "UNFOLLOWED", "您当前的微信尚未关注公众号，或已取消关注");
        }
        return unknown();
    }

    private MpVO unknown() {
        return result(1501, "UNKNOWN", "暂时无法确认公众号关注状态");
    }

    private MpVO result(int code, String state, String message) {
        MpVO result = new MpVO();
        result.setCode(code);
        result.setFollowState(state);
        result.setMsg(message);
        return result;
    }
}
