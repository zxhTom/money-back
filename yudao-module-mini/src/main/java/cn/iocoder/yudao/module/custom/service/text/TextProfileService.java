package cn.iocoder.yudao.module.custom.service.text;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.custom.controller.admin.text.vo.TextProfilePageReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.text.vo.TextProfileSaveReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.text.TextProfileDO;

public interface TextProfileService {

    /** 获得文案配置分页 */
    PageResult<TextProfileDO> getTextProfilePage(TextProfilePageReqVO reqVO);

    /** 获得文案配置详情 */
    TextProfileDO getTextProfile(Long id);

    /** 创建文案套（新建时 code 自动生成、isActive 固定为 false、seedFrom 固定为 safe） */
    Long createTextProfile(TextProfileSaveReqVO reqVO);

    /** 更新文案套（只能改 name/remark） */
    void updateTextProfile(TextProfileSaveReqVO reqVO);

    /** 删除文案套（当前生效文案套禁止删除；级联删除该文案套下所有文案条目） */
    void deleteTextProfile(Long id);

    /** 切换生效文案套（事务方法：先清空全部生效标记，再设置目标为生效） */
    void useTextProfile(Long id);

    /** 克隆一份文案套，包含全部文案条目 */
    Long cloneProfile(Long sourceId, String newName);

    /** 获得当前生效的文案套，不存在返回 null */
    TextProfileDO getActiveTextProfile();

}
