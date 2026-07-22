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

    /** 创建文案套（新建时 code 自动生成、isActive 固定为 false、seedFrom 固定为 safe、textMode 取自请求且创建后不可变） */
    Long createTextProfile(TextProfileSaveReqVO reqVO);

    /** 更新文案套（只能改 name/remark，textMode 创建后不可变，即使请求体带了也忽略） */
    void updateTextProfile(TextProfileSaveReqVO reqVO);

    /** 删除文案套（当前生效文案套禁止删除；级联删除该文案套下所有文案条目） */
    void deleteTextProfile(Long id);

    /** 切换生效文案套（事务方法：只清空目标所属 textMode 内的生效标记，再设置目标为生效；不影响其他 textMode） */
    void useTextProfile(Long id);

    /** 克隆一份文案套，包含全部文案条目；textMode 继承自来源 profile */
    Long cloneProfile(Long sourceId, String newName);

    /** 获得指定 textMode 内当前生效的文案套，不存在返回 null */
    TextProfileDO getActiveTextProfile(String textMode);

}
