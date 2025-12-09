package cn.iocoder.yudao.module.custom.controller.admin.wechat;

import cn.iocoder.yudao.module.custom.controller.admin.wechat.vo.MiniAppPath;
import cn.iocoder.yudao.module.custom.controller.admin.wechat.vo.TemplateVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.wechat.MiniUserDo;
import cn.iocoder.yudao.module.custom.dto.ApiResponse;
import cn.iocoder.yudao.module.custom.dto.Code2SessionResponse;
import cn.iocoder.yudao.module.custom.service.wechat.MiniUserService;
import cn.iocoder.yudao.module.custom.service.wechat.WechatService;
import cn.iocoder.yudao.module.custom.service.wechat.model.CombineUser;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.AuthLoginReqVO;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.AuthLoginRespVO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.auth.AdminAuthService;
import com.alibaba.fastjson.JSON;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.PermitAll;
import java.util.UUID;

/**
 * @author zxhtom
 * 10/3/25
 */
@Schema(description = "mini app login authorization interfaces")
@RestController
@RequestMapping(value = "/wechat")
@Slf4j
@PermitAll
public class WechatLoginController {
    @Autowired
    private WechatService wechatService;
    @Autowired
    MiniUserService miniUserService;
    @Autowired
    AdminAuthService authService;
    @Autowired
    @Lazy
    PasswordEncoder passwordEncoder;

    @Autowired
    private WxMpService wxMpService;

    @PostMapping("/send")
    public String send(@RequestBody TemplateVO templateVO) throws Exception {
        return wechatService.send(templateVO);
    }
    @PostMapping("/auto")
    public ApiResponse auto(@RequestBody WechatLoginRequest request) {

        AuthLoginReqVO loginRequest = new AuthLoginReqVO();
        loginRequest.setUsername(request.getUserInfo().getUsername());
        AuthLoginRespVO loginResponse = authService.authenticateUserNameOnly(loginRequest);

        // 3. 生成自定义登录态 (Token)
        String token = String.format("%s", loginResponse.getAccessToken());

        return ApiResponse.success(loginResponse);
    }

    @PostMapping("/generator")
    public ApiResponse generator(@RequestBody MiniAppPath miniAppPath) {
        try {
            String res = wechatService.generateUrlLink(miniAppPath.getPath(), miniAppPath.getQuery());
            return ApiResponse.success(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ApiResponse.error("error");
    }
    @PostMapping("/bindMiniApp")
    public ApiResponse bind(@RequestBody WechatLoginRequest request) {
        // 1. 通过 code 获取 openid
        Code2SessionResponse sessionResponse = wechatService.code2Session(request.getCode());
        String openid = sessionResponse.getOpenid();
        String unionid = sessionResponse.getUnionid();
        MiniUserDo combineUser = miniUserService.bindMinUser(openid, unionid);
        return ApiResponse.success(true);
    }
    @PostMapping("/login")
    public ApiResponse login(@RequestBody WechatLoginRequest request) {

        // 1. 通过 code 获取 openid
        Code2SessionResponse sessionResponse = wechatService.code2Session(request.getCode());

        if (!sessionResponse.isSuccess()) {
            return ApiResponse.error("微信登录失败: " + sessionResponse.getErrmsg());
        }
        log.info(JSON.toJSONString(sessionResponse));
        String openid = sessionResponse.getOpenid();
        String unionid = sessionResponse.getUnionid();
        CombineUser combineUser = miniUserService.selectMiniUserOrInitUserWithPrefix(request.getAppId(), openid,unionid, "wechat");
        // 2. 业务逻辑：根据 openid 查找或创建用户
        AdminUserDO user = combineUser.getMaltcloud();
        AuthLoginReqVO loginRequest = new AuthLoginReqVO();
        loginRequest.setUsername(user.getUsername());
        user.setPassword("");
        AuthLoginRespVO loginResponse = authService.authenticateUserNameOnly(loginRequest);

        // 3. 生成自定义登录态 (Token)
        String token = String.format("%s", loginResponse.getAccessToken());

        // 4. (可选) 将 token 和 user 的关联关系存储到 Redis 或数据库
        // redisTemplate.opsForValue().set("token:" + token, openid, Duration.ofDays(7));

        // 5. 返回 token 和用户信息给前端
        WechatLoginResponse wechatLoginResponse = new WechatLoginResponse(token, user,combineUser.isRegisted(),openid);
        return ApiResponse.success(wechatLoginResponse);
    }


    private String generateToken(AdminUserDO user) {
        // 生成一个随机的、安全的 Token
        return UUID.randomUUID().toString().replace("-", "");
    }

    // 内部类：接收前端请求
    @Data
    public static class WechatLoginRequest {
        private String appId;
        private String code;
        private AdminUserDO userInfo; // 可以是一个Map或自定义DTO
    }

    // 内部类：返回给前端的响应
    @Data
    @AllArgsConstructor
    public static class WechatLoginResponse {
        private String accessToken;
        private AdminUserDO user;
        private boolean registed;
        private String openid;
    }
}
