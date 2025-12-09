package cn.iocoder.yudao.module.custom.service.face.baidu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zxhtom
 * 12/3/25
 */
public interface BaiduFaceAuthService {

    public String getVerifyToken(String successCallbackUrl, String failCallbackUrl) throws Exception ;

    /**
     * 2. 上报指定用户的姓名和身份证号[citation:4]
     * @param verifyToken 上一步获取的token
     * @param name 真实姓名
     * @param idCard 身份证号码
     */
    public boolean reportUserInfo(String verifyToken, String name, String idCard) throws Exception ;

    public Map<String, Object> queryFaceAuthResult(String verifyToken) throws Exception ;

}
