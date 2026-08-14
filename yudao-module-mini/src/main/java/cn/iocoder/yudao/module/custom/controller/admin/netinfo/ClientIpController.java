package cn.iocoder.yudao.module.custom.controller.admin.netinfo;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 返回调用方自己的出口 IP，供小程序「我的IP」使用。
 *
 * 只登录即可访问、不挂 @PreAuthorize：小程序端是普通终端用户，没有后台权限。
 * 返回的是服务端亲眼看到的对端地址（IP黑白名单匹配的也是这个值），
 * 不是客户端内网地址——小程序和浏览器都拿不到内网 IP。
 */
@Tag(name = "管理后台 - 客户端网络信息")
@RestController
@RequestMapping("/custom/client-ip")
public class ClientIpController {

    @GetMapping("/get")
    @Operation(summary = "获取当前请求方的出口IP")
    public CommonResult<String> get(HttpServletRequest request) {
        return success(ServletUtils.getClientRealIp(request));
    }

}
