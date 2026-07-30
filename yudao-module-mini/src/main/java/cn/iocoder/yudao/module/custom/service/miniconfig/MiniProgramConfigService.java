package cn.iocoder.yudao.module.custom.service.miniconfig;

import cn.iocoder.yudao.module.custom.controller.admin.miniconfig.vo.MiniProgramConfigRespVO;
import cn.iocoder.yudao.module.custom.controller.admin.miniconfig.vo.MiniProgramConfigSaveReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.miniconfig.MiniProgramConfigDO;

public interface MiniProgramConfigService {

    /** 小程序端调用：只返回展示字段 */
    MiniProgramConfigRespVO getPublic();

    /** 管理端调用：返回完整配置（含 boundUserId） */
    MiniProgramConfigDO getAdmin();

    /**
     * 保存配置。仅当 appName 相对已保存的值发生变化、且新的 boundUserId 不为空时，
     * 联动更新该用户的 realname + 其名下所有合同（不限状态）的当事人姓名。
     * 换绑不回滚旧用户/旧合同。
     */
    void update(MiniProgramConfigSaveReqVO reqVO);

    /**
     * 查：如果保存后绑定的是 boundUserId 这个用户，名下有多少条合同会受"改名"影响
     * （与新名称具体是什么无关，只看数量）。
     * boundUserId 必须传"即将保存的绑定用户"，不能默认取当前已保存的绑定用户——
     * 否则改名同时换绑时，预览的是旧用户的合同数，跟实际会联动到的新用户对不上。
     * 传 null 表示"保存后将解绑"，此时直接返回 0。
     */
    int previewNameChangeImpact(Long boundUserId);

}
