package cn.iocoder.yudao.module.custom.service.wechat.msg.impl;

import cn.iocoder.yudao.module.custom.service.wechat.msg.MessageHandler;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MsgSubscribeHandler implements MessageHandler {
    @Override
    public boolean canHandle(String msgType) {
        return "event".equals(msgType);
    }

    @Override
    public String handle(Map<String, String> message) {
        // 处理订阅事件
        return "处理关注事件";
    }
}
