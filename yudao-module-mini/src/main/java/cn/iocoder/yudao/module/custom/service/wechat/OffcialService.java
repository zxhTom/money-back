package cn.iocoder.yudao.module.custom.service.wechat;

import javax.servlet.http.HttpServletRequest;

public interface OffcialService {

    public String getAccessToken() ;

    public boolean checkSignature(String signature, String timestamp, String nonce);

    public String processMessage(String requestBody, HttpServletRequest request) ;
}
