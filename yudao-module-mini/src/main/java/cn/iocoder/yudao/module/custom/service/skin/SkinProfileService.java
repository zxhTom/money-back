package cn.iocoder.yudao.module.custom.service.skin;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.custom.controller.admin.skin.vo.SkinCloneReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.skin.vo.SkinProfilePageReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.skin.vo.SkinProfileSaveReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.skin.SkinProfileDO;

public interface SkinProfileService {

    /** 获得皮肤配置分页 */
    PageResult<SkinProfileDO> getSkinProfilePage(SkinProfilePageReqVO reqVO);

    /** 获得皮肤配置详情 */
    SkinProfileDO getSkinProfile(Long id);

    /** 创建皮肤配置（新建时 type 固定为 1 自定义、isActive 固定为 false） */
    Long createSkinProfile(SkinProfileSaveReqVO reqVO);

    /** 更新皮肤配置（预设皮肤禁止修改核心字段） */
    void updateSkinProfile(SkinProfileSaveReqVO reqVO);

    /** 删除皮肤配置（预设皮肤、当前生效皮肤禁止删除） */
    void deleteSkinProfile(Long id);

    /** 切换生效皮肤（事务方法：先清空全部生效标记，再设置目标为生效） */
    void useSkinProfile(Long id);

    /** 基于预设皮肤克隆一份自定义皮肤 */
    Long cloneAsCustom(SkinCloneReqVO reqVO);

    /** 获得当前生效的皮肤配置，不存在返回 null */
    SkinProfileDO getActiveSkinProfile();

}
