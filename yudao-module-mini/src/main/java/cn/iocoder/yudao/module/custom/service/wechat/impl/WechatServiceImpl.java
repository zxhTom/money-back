package cn.iocoder.yudao.module.custom.service.wechat.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.urllink.GenerateUrlLinkRequest;
import cn.binarywang.wx.miniapp.constant.WxMaConstants;
import cn.hutool.core.collection.CollectionUtil;
import cn.iocoder.yudao.module.custom.config.WechatConfig;
import cn.iocoder.yudao.module.custom.controller.admin.wechat.vo.TemplateVO;
import cn.iocoder.yudao.module.custom.dal.mysql.custom.CustomDefineMapper;
import cn.iocoder.yudao.module.custom.dto.Code2SessionResponse;
import cn.iocoder.yudao.module.custom.service.wechat.OffcialService;
import cn.iocoder.yudao.module.custom.service.wechat.WechatService;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * @author zxhtom
 * 10/3/25
 */
@Service
public class WechatServiceImpl implements WechatService {

    @Autowired
    WxMpService wxMpService;
    @Autowired
    CustomDefineMapper customDefineMapper;
    @Autowired
    private WechatConfig wechatConfig;
    @Autowired
    WxMaService wxMaService;
    @Autowired
    AdminUserService adminUserService;
    @Autowired
    OffcialService offcialService;

    private static final String CODE2SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";
    private static final String GET_VERSION_INFO_URL = "https://api.weixin.qq.com/wxa/getversioninfo";

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
    /**
     * 生成小程序URL Link
     * @param pagePath 小程序页面路径，例如：/pages/authResult/index
     * @param query 页面参数，例如：status=success&verify_token=xxx
     * @return 生成的URL Link (格式如：https://wxaurl.cn/xxxxx)
     */
    @Override
    public String generateUrlLink(String pagePath, String query) throws Exception {

        // 1. 构建请求参数[citation:1]
        GenerateUrlLinkRequest request = GenerateUrlLinkRequest.builder()
                .path(pagePath) // 要跳转的小程序页面路径[citation:5]
                .query(query)   // 页面参数，最大1024字符[citation:5]
                .envVersion(WxMaConstants.DEFAULT_ENV_VERSION) // 默认"release"正式版[citation:1]
                .expireType(1) // 失效类型：1-按间隔天数失效[citation:5]
                .expireInterval(7) // 7天后链接失效，最长30天[citation:5]
                .build();

        // 2. 调用SDK生成链接[citation:1]
        return wxMaService.getLinkService().generateUrlLink(request);
    }

    @Override
    public Integer updateVerify(String idCard, int verified) {
        return customDefineMapper.updateVerify(idCard,verified);
    }

    @Override
    public String send(TemplateVO templateVO) throws WxErrorException {
        List<AdminUserDO> userListByRealname = adminUserService.getUserByIdNo(templateVO.getIdNo());
        if (CollectionUtil.isEmpty(userListByRealname)) {
            throw new RuntimeException("user empty");
        }
        String openId=customDefineMapper.selectOffcialOpenIdByUserId(userListByRealname.get(0).getId());
        WxMpTemplateMessage message = WxMpTemplateMessage.builder()
                .toUser(openId)
                .templateId(templateVO.getTemplateId())
                .build();
        if (CollectionUtil.isNotEmpty(templateVO.getDatas())) {
            for (Map.Entry<String, Object> entry : templateVO.getDatas().entrySet()) {
                message.addData(new WxMpTemplateData(entry.getKey(), entry.getValue().toString()));
            }
        }
        message.addData(new WxMpTemplateData("first", "测试模板消息"));
        message.addData(new WxMpTemplateData("keyword1", "内容1"));
        message.addData(new WxMpTemplateData("remark", "结束"));

        return wxMpService.getTemplateMsgService().sendTemplateMsg(message);
    }

    /**
     * 查询小程序线上版本号
     * 使用微信开放平台API：https://api.weixin.qq.com/wxa/getversioninfo
     * 
     * 注意：此接口需要使用小程序的 access_token，而不是公众号的 access_token
     * 
     * @return 小程序版本号（线上版本）
     * @throws Exception 如果查询失败
     */
    @Override
    public String getMiniProgramVersion() throws Exception {
        // 1. 获取小程序的 access_token（使用 WxMaService）
        String accessToken;
        try {
            accessToken = wxMaService.getAccessToken();
        } catch (Exception e) {
            // 如果 WxMaService 获取失败，尝试直接调用 API
            accessToken = getMiniProgramAccessToken();
        }
        
        // 2. 调用微信API查询版本信息（需要使用 POST 方法）
        String url = String.format("%s?access_token=%s", GET_VERSION_INFO_URL, accessToken);
        RestTemplate restTemplate = new RestTemplate();
        // 使用 POST 方法，body 为空 JSON 对象
        String response = restTemplate.postForObject(url, "{}", String.class);
        
        // 3. 解析响应
        JSONObject json = JSON.parseObject(response);
        
        // 4. 检查是否有错误
        if (json.containsKey("errcode") && json.getIntValue("errcode") != 0) {
            int errcode = json.getIntValue("errcode");
            String errmsg = json.getString("errmsg");
            throw new RuntimeException("查询小程序版本号失败: " + errcode + " - " + errmsg);
        }
        
        // 5. 获取版本列表，找到线上版本（正式版）
        if (json.containsKey("version_list") && json.getJSONArray("version_list") != null) {
            List<JSONObject> versionList = json.getJSONArray("version_list").toJavaList(JSONObject.class);
            
            // 查找线上版本（正式版），通常是最新的版本
            // 微信返回的版本列表按时间倒序，第一个通常是线上版本
            if (!versionList.isEmpty()) {
                JSONObject onlineVersion = versionList.get(0);
                // 返回版本号，字段可能是 version 或 user_version
                if (onlineVersion.containsKey("version")) {
                    return onlineVersion.getString("version");
                } else if (onlineVersion.containsKey("user_version")) {
                    return onlineVersion.getString("user_version");
                }
            }
        }
        
        // 如果没有找到版本信息，返回空或抛出异常
        throw new RuntimeException("未找到小程序线上版本号");
    }

    /**
     * 直接调用微信API获取小程序的 access_token
     * 
     * @return 小程序的 access_token
     */
    private String getMiniProgramAccessToken() {
        String appid = wechatConfig.getAppid();
        String secret = wechatConfig.getSecret();
        String url = String.format(
                "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
                appid, secret
        );

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);

        JSONObject json = JSON.parseObject(response);
        if (json.containsKey("access_token")) {
            return json.getString("access_token");
        } else {
            int errcode = json.getIntValue("errcode");
            String errmsg = json.getString("errmsg");
            throw new RuntimeException("获取小程序Token失败: " + errcode + " - " + errmsg);
        }
    }
}
