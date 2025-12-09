package cn.iocoder.yudao.module.custom.service.wechat.impl;

import cn.iocoder.yudao.module.custom.service.wechat.OffcialService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.binarywang.spring.starter.wxjava.mp.properties.WxMpProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class OffcialServiceImpl implements OffcialService {
    @Autowired
    private WxMpProperties wxMpProperties;
    @Autowired
    RedisTemplate redisTemplate;
    private static final String TOKEN_KEY_PREFIX = "wechat:access_token:";
    private static final String TOKEN_LOCK_KEY = "wechat:token_lock:";
    /**
     * 获取 Token（线程安全，支持分布式）
     */
    @Override
    public String getAccessToken() {
        String appId = wxMpProperties.getAppId();
        String tokenKey = TOKEN_KEY_PREFIX + appId;

        // 1. 尝试从 Redis 获取
        String token = (String) redisTemplate.opsForValue().get(tokenKey);
        if (token != null && !token.isEmpty()) {
            return token;
        }

        // 2. 获取分布式锁，防止并发刷新
        String lockKey = TOKEN_LOCK_KEY + appId;
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(
                lockKey, "locked", 30, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(lockAcquired)) {
            try {
                // 3. 再次检查（防止拿到锁时已被其他线程刷新）
                token = (String) redisTemplate.opsForValue().get(tokenKey);
                if (token != null) {
                    return token;
                }

                // 4. 调用微信 API 获取新 Token
                String newToken = fetchNewAccessToken();

                // 5. 存入 Redis，设置过期时间（提前5分钟过期）
                redisTemplate.opsForValue().set(
                        tokenKey,
                        newToken,
                        115, // 微信返回7200秒，这里设为115分钟
                        TimeUnit.MINUTES
                );

                return newToken;
            } finally {
                // 释放锁
                redisTemplate.delete(lockKey);
            }
        } else {
            // 等待其他线程刷新完成
            try {
                Thread.sleep(100);
                return getAccessToken(); // 重试
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("获取Token中断", e);
            }
        }
    }

    /**
     * 调用微信 API 获取新 Token
     */
    private String fetchNewAccessToken() {
        String appId = wxMpProperties.getAppId();
        String secret = wxMpProperties.getSecret();
        String url = String.format(
                "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
                appId, secret
        );

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);

        JSONObject json = JSON.parseObject(response);
        if (json.containsKey("access_token")) {
            return json.getString("access_token");
        } else {
            int errcode = json.getIntValue("errcode");
            String errmsg = json.getString("errmsg");
            throw new RuntimeException("获取微信Token失败: " + errcode + " - " + errmsg);
        }
    }

    /**
     * 强制刷新 Token
     */
    public String refreshAccessToken() {
        String appId = wxMpProperties.getAppId();
        String tokenKey = TOKEN_KEY_PREFIX + appId;
        String newToken = fetchNewAccessToken();

        redisTemplate.opsForValue().set(
                tokenKey,
                newToken,
                115,
                TimeUnit.MINUTES
        );

        return newToken;
    }
    /**
     * 验证微信签名
     */
    @Override
    public boolean checkSignature(String signature, String timestamp, String nonce) {
        String[] arr = new String[]{wxMpProperties.getToken(), timestamp, nonce};
        Arrays.sort(arr);

        StringBuilder content = new StringBuilder();
        for (String s : arr) {
            content.append(s);
        }

        String temp = sha1(content.toString());
        return temp != null && temp.equals(signature);
    }

    /**
     * SHA1加密
     */
    private String sha1(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(str.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String shaHex = Integer.toHexString(b & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 处理微信消息
     */
    @Override
    public String processMessage(String requestBody, HttpServletRequest request) {
        try {
            // 解析XML消息
            Map<String, String> messageMap = parseXml(requestBody);

            String msgType = messageMap.get("MsgType");
            String fromUser = messageMap.get("FromUserName");
            String toUser = messageMap.get("ToUserName");

            // 处理事件消息
            if ("event".equals(msgType)) {
                String event = messageMap.get("Event");
                return handleEvent(event, fromUser, toUser, messageMap);
            }

            // 处理文本消息
            if ("text".equals(msgType)) {
                String content = messageMap.get("Content");
                return handleTextMessage(fromUser, toUser, content);
            }

            // 其他类型消息
            return buildTextResponse(fromUser, toUser, "暂不支持此消息类型");

        } catch (Exception e) {
            e.printStackTrace();
            return "success";
        }
    }

    /**
     * 处理关注/取消关注事件
     */
    private String handleEvent(String event, String fromUser, String toUser,
                              Map<String, String> messageMap) {
        switch (event) {
            case "subscribe": // 关注事件
                return handleSubscribeEvent(fromUser, toUser);
            case "unsubscribe": // 取消关注
                return handleUnsubscribeEvent(fromUser, toUser);
            case "SCAN": // 扫码关注（已关注用户扫码）
                return handleScanEvent(fromUser, toUser, messageMap);
            default:
                return "success";
        }
    }

    /**
     * 处理关注事件
     */
    private String handleSubscribeEvent(String fromUser, String toUser) {
        // 这里可以记录用户关注信息到数据库
        // userService.saveOrUpdateWeChatUser(fromUser);

        // 构建欢迎消息
        String welcomeMsg = "欢迎关注！\n" +
                           "回复以下关键词：\n" +
                           "1. 查看最新活动\n" +
                           "2. 联系客服\n" +
                           "3. 使用帮助";

        return buildTextResponse(fromUser, toUser, welcomeMsg);
    }

    /**
     * 处理取消关注事件
     */
    private String handleUnsubscribeEvent(String fromUser, String toUser) {
        // 更新用户状态为取消关注
        // userService.updateUserStatus(fromUser, "unsubscribed");
        return "success";
    }

    /**
     * 处理扫码事件
     */
    private String handleScanEvent(String fromUser, String toUser,
                                  Map<String, String> messageMap) {
        String eventKey = messageMap.get("EventKey"); // 扫码场景值
        String ticket = messageMap.get("Ticket");

        // 根据不同的场景值处理业务逻辑
        String responseMsg = "欢迎回来！扫码场景值：" + eventKey;
        return buildTextResponse(fromUser, toUser, responseMsg);
    }

    /**
     * 处理文本消息
     */
    private String handleTextMessage(String fromUser, String toUser, String content) {
        String response;
        switch (content.trim()) {
            case "1":
                response = "最新活动：\n1. 优惠活动正在进行中\n2. 新品上市\n3. 会员专享";
                break;
            case "2":
                response = "客服电话：400-123-4567\n客服微信：service123";
                break;
            case "3":
                response = "使用帮助：\n回复数字查看对应内容\n1-最新活动\n2-联系客服";
                break;
            default:
                response = "已收到您的消息：" + content + "\n回复数字查看：\n1.最新活动\n2.联系客服\n3.使用帮助";
        }
        return buildTextResponse(fromUser, toUser, response);
    }

    /**
     * 构建文本消息响应
     */
    private String buildTextResponse(String fromUser, String toUser, String content) {
        return String.format(
            "<xml>" +
            "<ToUserName><![CDATA[%s]]></ToUserName>" +
            "<FromUserName><![CDATA[%s]]></FromUserName>" +
            "<CreateTime>%s</CreateTime>" +
            "<MsgType><![CDATA[text]]></MsgType>" +
            "<Content><![CDATA[%s]]></Content>" +
            "</xml>",
            fromUser, toUser, System.currentTimeMillis() / 1000, content
        );
    }

    /**
     * 解析XML消息
     */
    private Map<String, String> parseXml(String xml) throws Exception {
        Map<String, String> map = new HashMap<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xml)));

        Element root = doc.getDocumentElement();
        NodeList nodeList = root.getChildNodes();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                map.put(node.getNodeName(), node.getTextContent());
            }
        }
        return map;
    }
}
