package cn.iocoder.yudao.module.custom.service.wechat.msg;

import java.util.Map;

public interface MessageHandler {
    boolean canHandle(String msgType);
    String handle(Map<String, String> message);
}
