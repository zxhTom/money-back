package cn.iocoder.yudao.module.custom.service.wechat.impl;

import cn.iocoder.yudao.module.custom.config.WechatConfig;
import cn.iocoder.yudao.module.custom.dto.Code2SessionResponse;
import cn.iocoder.yudao.module.custom.service.wechat.WechatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * @author zxhtom
 * 10/3/25
 */
@Service
public class WechatServiceImpl implements WechatService {

    @Autowired
    private WechatConfig wechatConfig;

    private static final String CODE2SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";

    /**
     * 使用 code 换取 openid 和 session_key
     * @param code 前端传来的 code
     * @return 微信接口的响应
     */
    @Override
    public Code2SessionResponse code2Session(String code) {
        RestTemplate restTemplate = new RestTemplate();

        // 构造请求URL
        String url = String.format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                CODE2SESSION_URL, wechatConfig.getAppid(), wechatConfig.getSecret(), code);

        // 发送GET请求
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // 解析JSON响应
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(response.getBody(), Code2SessionResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("解析微信响应失败", e);
        }
    }

}
