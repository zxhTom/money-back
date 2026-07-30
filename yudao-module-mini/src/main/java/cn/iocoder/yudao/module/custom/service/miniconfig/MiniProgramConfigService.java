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

    /** 查：如果现在绑定的用户名下有多少条合同会受"改名"影响（与新名称具体是什么无关，只看数量） */
    int previewNameChangeImpact();

}
