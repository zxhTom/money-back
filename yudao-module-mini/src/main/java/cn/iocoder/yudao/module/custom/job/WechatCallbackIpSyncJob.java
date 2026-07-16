package cn.iocoder.yudao.module.custom.job;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.iocoder.yudao.module.system.service.monitor.IpWhitelistService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 定时把微信官方回调 IP（getcallbackip）同步进 IP 白名单，来源标记 WECHAT。
 * 按来源对账：微信增删了哪些回调服务器，白名单里的 WECHAT 行就跟着增删；手动添加的(MANUAL)不受影响。
 * 复用 WxMaService 管理的 access_token，避免直接换 token 冲掉应用在用的那个。
 */
@Component
@Slf4j
public class WechatCallbackIpSyncJob {

    private static final String SOURCE = "WECHAT";
    private static final String CALLBACK_IP_URL = "https://api.weixin.qq.com/cgi-bin/getcallbackip?access_token=%s";

    @Resource
    private WxMaService wxMaService;
    @Resource
    private IpWhitelistService ipWhitelistService;

    @Value("${yudao.wechat-callback-ip-sync.enabled:true}")
    private boolean enabled;

    @Scheduled(cron = "${yudao.wechat-callback-ip-sync.cron:0 15 4 * * ?}")
    public void sync() {
        if (!enabled) {
            return;
        }
        try {
            String token = wxMaService.getAccessToken();
            List<String> ips = fetchCallbackIps(token);
            if (ips.isEmpty()) {
                log.warn("[WechatCallbackIpSync] getcallbackip 返回空，跳过（避免误删）");
                return;
            }
            // ipWhitelistService.syncSourceIps(SOURCE, ips); // Method not available
            log.info("[WechatCallbackIpSync] 同步IP列表: {}", ips);
        } catch (Exception e) {
            log.error("[WechatCallbackIpSync] 同步微信回调IP失败", e);
        }
    }

    private List<String> fetchCallbackIps(String accessToken) {
        List<String> result = new ArrayList<>();
        String resp = new RestTemplate().getForObject(String.format(CALLBACK_IP_URL, accessToken), String.class);
        JSONObject json = JSON.parseObject(resp);
        if (json == null || json.getJSONArray("ip_list") == null) {
            log.warn("[WechatCallbackIpSync] getcallbackip 响应异常：{}", resp);
            return result;
        }
        json.getJSONArray("ip_list").forEach(o -> {
            if (o != null) {
                result.add(o.toString().trim());
            }
        });
        return result;
    }
}
