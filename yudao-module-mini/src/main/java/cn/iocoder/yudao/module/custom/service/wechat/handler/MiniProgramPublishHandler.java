package cn.iocoder.yudao.module.custom.service.wechat.handler;

import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractModelDO;
import cn.iocoder.yudao.module.custom.dal.mysql.contract.ContractModelMapper;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 小程序发布成功事件处理器
 * 
 * 处理微信开放平台推送的小程序发布成功事件
 * 事件类型：Event = "weapp_audit_success"
 * 
 * 业务逻辑：
 * 1. 获取小程序版本号
 * 2. 在 contract_model 表中新增或更新记录
 *    - 如果 app_version 已存在，则更新 model 为 'offcial'，create_time 为当前时间
 *    - 如果不存在，则新增一条记录，model 为 'offcial'，create_time 为当前时间
 * 
 * @author zxhtom
 */
@Component
@Slf4j
public class MiniProgramPublishHandler implements WxMpMessageHandler {

    @Autowired
    private ContractModelMapper contractModelMapper;

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context,
                                    WxMpService wxMpService, WxSessionManager sessionManager) {
        log.info("[handle][接收到小程序发布成功事件，消息：{}]", wxMessage);
        
        try {
            // 获取事件类型和相关信息
            String event = wxMessage.getEvent();
            String fromUser = wxMessage.getFromUser();
            String toUser = wxMessage.getToUser();
            
            // 检查是否是小程序发布成功事件
            // 微信开放平台推送的小程序发布成功事件，Event 是 "weapp_audit_success"
            if ("weapp_audit_success".equals(event)) {
                
                log.info("[handle][检测到小程序发布成功事件 - Event: {}, FromUser: {}, ToUser: {}]", 
                        event, fromUser, toUser);
                
                // 处理小程序发布成功的业务逻辑
                handleMiniProgramPublishSuccess(wxMessage, wxMpService);
                
                // 返回空消息，表示已处理
                return null;
            }
            
            // 如果不是小程序发布成功事件，返回 null 让其他 Handler 处理
            return null;
            
        } catch (Exception e) {
            log.error("[handle][处理小程序发布成功事件失败]", e);
            return null;
        }
    }

    /**
     * 处理小程序发布成功的业务逻辑
     * 
     * @param wxMessage 微信消息对象
     * @param wxMpService 微信服务对象
     */
    private void handleMiniProgramPublishSuccess(WxMpXmlMessage wxMessage, WxMpService wxMpService) {
        try {
            String appId = wxMpService.getWxMpConfigStorage().getAppId();
            String event = wxMessage.getEvent();
            
            log.info("[handleMiniProgramPublishSuccess][开始处理小程序发布成功事件 - AppId: {}, Event: {}]", 
                    appId, event);
            
            // 获取小程序版本号
            // 注意：微信推送的小程序发布成功事件中，版本号可能在 EventKey 或其他字段中
            // 这里需要根据实际的微信推送格式来解析版本号
            // 如果 EventKey 包含版本号，可以从那里获取；否则可能需要从其他字段获取
            String appVersion = extractAppVersion(wxMessage);
            
            if (appVersion == null || appVersion.isEmpty()) {
                log.warn("[handleMiniProgramPublishSuccess][无法获取小程序版本号，跳过处理]");
                return;
            }
            
            log.info("[handleMiniProgramPublishSuccess][小程序版本号: {}]", appVersion);
            
            // 查询 contract_model 表中是否已存在该版本号
            ContractModelDO existingModel = contractModelMapper.selectByAppVersion(appVersion);
            
            LocalDateTime now = LocalDateTime.now();
            
            if (existingModel != null) {
                // 如果已存在，则更新
                existingModel.setModel("offcial");
                existingModel.setCreateTime(now);
                // 注意：更新时 createTime 需要手动设置，因为 BaseDO 的 createTime 是 INSERT 时自动填充
                contractModelMapper.updateById(existingModel);
                log.info("[handleMiniProgramPublishSuccess][更新 contract_model 记录 - AppVersion: {}, Model: offcial]", 
                        appVersion);
            } else {
                // 如果不存在，则新增
                ContractModelDO newModel = new ContractModelDO();
                newModel.setAppVersion(appVersion);
                newModel.setModel("offcial");
                newModel.setCreateTime(now);
                contractModelMapper.insert(newModel);
                log.info("[handleMiniProgramPublishSuccess][新增 contract_model 记录 - AppVersion: {}, Model: offcial]", 
                        appVersion);
            }
            
            log.info("[handleMiniProgramPublishSuccess][小程序发布成功处理完成 - AppId: {}, AppVersion: {}]", 
                    appId, appVersion);
            
        } catch (Exception e) {
            log.error("[handleMiniProgramPublishSuccess][处理小程序发布成功事件失败]", e);
            throw new RuntimeException("处理小程序发布成功事件失败", e);
        }
    }

    /**
     * 从微信消息中提取小程序版本号
     * 
     * 微信开放平台推送的小程序发布成功事件，版本号可能在以下字段：
     * 1. EventKey: 可能包含版本号或版本相关信息
     * 2. 消息的原始 XML 中可能有 version 或 app_version 字段
     * 3. 如果无法从消息中获取，可以尝试从微信 API 获取最新版本号
     * 
     * 注意：实际使用时，需要根据微信推送的实际 XML 格式来调整此方法
     * 
     * @param wxMessage 微信消息对象
     * @return 小程序版本号
     */
    private String extractAppVersion(WxMpXmlMessage wxMessage) {
        // 方式1：从 EventKey 获取（如果版本号在 EventKey 中）
        String eventKey = wxMessage.getEventKey();
        if (eventKey != null && !eventKey.isEmpty()) {
            // EventKey 可能直接是版本号，或者包含版本号信息
            // 如果 EventKey 格式是 "version:1.0.0" 或类似，需要解析
            // 这里先尝试直接使用 EventKey，如果格式不对，后续可以根据实际情况调整
            log.info("[extractAppVersion][从 EventKey 获取版本号: {}]", eventKey);
            return eventKey;
        }
        
        // 方式2：尝试从消息内容中解析
        // 微信小程序发布成功事件可能包含版本号信息
        // 可以通过 wxMessage 的其他方法获取，或者解析原始 XML
        String content = wxMessage.getContent();
        if (content != null && !content.isEmpty()) {
            // 如果内容中包含版本号，可以尝试解析
            log.info("[extractAppVersion][从 Content 获取: {}]", content);
            // 这里可以根据实际格式解析，例如正则表达式提取版本号
        }
        
        // 方式3：如果无法从消息中获取，记录警告
        // 实际使用时，可能需要：
        // - 查看微信推送的实际 XML 格式
        // - 或者调用微信 API 获取最新版本号
        log.warn("[extractAppVersion][无法从消息中提取版本号，EventKey: {}, Content: {}, 消息: {}]", 
                eventKey, content, wxMessage);
        
        // 临时方案：如果无法获取，返回 null，由业务层决定如何处理
        // 实际使用时，建议根据微信推送的实际格式调整此方法
        // 或者可以调用微信 API 获取最新版本号：
        // wxMpService.getWxMpConfigStorage().getAppId() 获取 appId
        // 然后调用微信 API 获取最新版本号
        return null;
    }
}

