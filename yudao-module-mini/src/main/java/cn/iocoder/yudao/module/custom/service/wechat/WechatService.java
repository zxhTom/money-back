package cn.iocoder.yudao.module.custom.service.wechat;


import cn.iocoder.yudao.module.custom.dto.Code2SessionResponse;

/**
 * @author zxhtom
 * 10/3/25
 */
public interface WechatService {

    public Code2SessionResponse code2Session(String code);
}
