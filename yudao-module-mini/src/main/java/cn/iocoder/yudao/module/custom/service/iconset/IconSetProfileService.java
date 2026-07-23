package cn.iocoder.yudao.module.custom.service.iconset;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.custom.controller.admin.iconset.vo.IconSetCloneReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.iconset.vo.IconSetProfilePageReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.iconset.vo.IconSetProfileSaveReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.iconset.IconSetProfileDO;

public interface IconSetProfileService {

    /** 获得图标集配置分页 */
    PageResult<IconSetProfileDO> getIconSetProfilePage(IconSetProfilePageReqVO reqVO);

    /** 获得图标集配置详情 */
    IconSetProfileDO getIconSetProfile(Long id);

    /** 创建图标集配置（新建时 type 固定为 1 自定义、isActive 固定为 false） */
    Long createIconSetProfile(IconSetProfileSaveReqVO reqVO);

    /** 更新图标集配置（预设图标集禁止修改核心字段 icons） */
    void updateIconSetProfile(IconSetProfileSaveReqVO reqVO);

    /** 删除图标集配置（预设图标集、当前生效图标集禁止删除） */
    void deleteIconSetProfile(Long id);

    /** 切换生效图标集（事务方法：先清空全部生效标记，再设置目标为生效） */
    void useIconSetProfile(Long id);

    /** 基于预设图标集克隆一份自定义图标集 */
    Long cloneAsCustom(IconSetCloneReqVO reqVO);

    /** 获得当前生效的图标集配置，不存在返回 null */
    IconSetProfileDO getActiveIconSetProfile();

}
