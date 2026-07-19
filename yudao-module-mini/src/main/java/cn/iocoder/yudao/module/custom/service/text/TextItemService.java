package cn.iocoder.yudao.module.custom.service.text;

import cn.iocoder.yudao.module.custom.controller.admin.text.vo.TextItemBatchUpdateReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.text.TextItemDO;

import java.util.List;

public interface TextItemService {

    /** 获得某文案套下的全部文案条目 */
    List<TextItemDO> listByProfile(Long profileId);

    /** 获得某文案套下某页面的文案条目列表 */
    List<TextItemDO> listByProfileAndPage(Long profileId, String pageKey);

    /** 在某文案套下按关键字模糊搜索文案条目（匹配 itemKey 或 itemValue） */
    List<TextItemDO> searchByKeyword(Long profileId, String keyword);

    /** 批量保存（upsert）某页面下的文案条目 */
    void batchUpdate(TextItemBatchUpdateReqVO reqVO);

}
