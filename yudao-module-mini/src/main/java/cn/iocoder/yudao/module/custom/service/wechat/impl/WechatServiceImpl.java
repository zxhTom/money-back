package cn.iocoder.yudao.module.custom.service.wechat.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.urllink.GenerateUrlLinkRequest;
import cn.binarywang.wx.miniapp.constant.WxMaConstants;
import cn.hutool.core.collection.CollectionUtil;
import cn.iocoder.yudao.module.custom.config.WechatConfig;
import cn.iocoder.yudao.module.custom.controller.admin.wechat.vo.TemplateVO;
import cn.iocoder.yudao.module.custom.dal.mysql.custom.CustomDefineMapper;
import cn.iocoder.yudao.module.custom.dto.Code2SessionResponse;
import cn.iocoder.yudao.module.custom.service.wechat.WechatService;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
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
        List<AdminUserDO> userListByRealname = adminUserService.getUserListByRealname(templateVO.getRealname());
        if (CollectionUtil.isEmpty(userListByRealname)) {
            throw new RuntimeException("user empty");
        }
        String openId=customDefineMapper.selectOffcialOpenIdByUserId(userListByRealname.get(0).getId());
        WxMpTemplateMessage message = WxMpTemplateMessage.builder()
                .toUser(openId)
                .templateId(templateVO.getTemplateId())
                .build();

        message.addData(new WxMpTemplateData("first", "测试模板消息"));
        message.addData(new WxMpTemplateData("keyword1", "内容1"));
        message.addData(new WxMpTemplateData("remark", "结束"));

        return wxMpService.getTemplateMsgService().sendTemplateMsg(message);
    }
}
