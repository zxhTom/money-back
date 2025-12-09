package cn.iocoder.yudao.module.custom.service.wechat;


import cn.iocoder.yudao.module.custom.controller.admin.wechat.vo.TemplateVO;
import cn.iocoder.yudao.module.custom.dto.Code2SessionResponse;
import me.chanjar.weixin.common.error.WxErrorException;

/**
 * @author zxhtom
 * 10/3/25
 */
public interface WechatService {

    public Code2SessionResponse code2Session(String code);

    public String generateUrlLink(String pagePath, String query) throws Exception;

    Integer updateVerify(String idCard, int verified);

    String send(TemplateVO templateVO) throws WxErrorException;
}
