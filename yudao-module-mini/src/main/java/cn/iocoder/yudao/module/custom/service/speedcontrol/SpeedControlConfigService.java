package cn.iocoder.yudao.module.custom.service.speedcontrol;

import cn.iocoder.yudao.module.custom.controller.admin.speedcontrol.vo.SpeedControlConfigRespVO;
import cn.iocoder.yudao.module.custom.controller.admin.speedcontrol.vo.SpeedControlConfigSaveReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.speedcontrol.SpeedControlConfigDO;

public interface SpeedControlConfigService {

    /** 供后台页面读取（实时查库） */
    SpeedControlConfigRespVO get();

    void update(SpeedControlConfigSaveReqVO reqVO);

    /**
     * 供过滤器每请求读取，走 10 秒内存快照，避免每个请求都查库。
     * 表为空/查库异常时返回 null，调用方按"不限速"处理（fail-open）。
     */
    SpeedControlConfigDO getCachedConfig();

    /** 黑名单式：默认全员受控，命中豁免用户或豁免角色才返回 true */
    boolean isExempt(Long userId);

}
