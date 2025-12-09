package cn.iocoder.yudao.module.custom.service.wechat.impl;

import cn.iocoder.yudao.module.custom.service.wechat.OffcialService;
import cn.iocoder.yudao.module.custom.service.wechat.WechatMenuService;
import cn.iocoder.yudao.module.custom.service.wechat.WechatService;
import cn.iocoder.yudao.module.custom.service.wechat.utils.ClasspathJsonReader;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class WechatMenuServiceImpl implements WechatMenuService {
    @Autowired
    WechatService wechatService;
    @Autowired
    OffcialService offcialService;
    @Autowired
    ClasspathJsonReader classpathJsonReader;

    @Override
    public String createMenu() {
        RestTemplate restTemplate = new RestTemplate();
        String accessToken = offcialService.getAccessToken();
        String url = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=" + accessToken;
        JSONObject menuJson = classpathJsonReader.readJsonObject("config/menu-config.json");

        // 发送POST请求
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(menuJson.toJSONString(), headers);
        String result = restTemplate.postForObject(url, request, String.class);
        log.info(result);
        return result;
    }
}
