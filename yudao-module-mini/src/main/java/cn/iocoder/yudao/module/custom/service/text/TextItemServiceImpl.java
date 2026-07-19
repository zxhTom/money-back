package cn.iocoder.yudao.module.custom.service.text;

import cn.iocoder.yudao.module.custom.controller.admin.text.vo.TextItemBatchUpdateReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.text.vo.TextItemSaveReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.text.TextItemDO;
import cn.iocoder.yudao.module.custom.dal.mysql.text.TextItemMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * 小程序文案条目 Service 实现类
 */
@Service
public class TextItemServiceImpl implements TextItemService {

    @Resource
    private TextItemMapper textItemMapper;

    @Override
    public List<TextItemDO> listByProfile(Long profileId) {
        return textItemMapper.selectListByProfileId(profileId);
    }

    @Override
    public List<TextItemDO> listByProfileAndPage(Long profileId, String pageKey) {
        return textItemMapper.selectListByProfileIdAndPageKey(profileId, pageKey);
    }

    @Override
    public List<TextItemDO> searchByKeyword(Long profileId, String keyword) {
        return textItemMapper.selectListByProfileIdAndKeyword(profileId, keyword);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdate(TextItemBatchUpdateReqVO reqVO) {
        if (CollectionUtils.isEmpty(reqVO.getItems())) {
            return;
        }
        for (TextItemSaveReqVO item : reqVO.getItems()) {
            TextItemDO existing = textItemMapper.selectByProfileIdAndItemKey(reqVO.getProfileId(), item.getItemKey());
            if (existing != null) {
                TextItemDO updateObj = new TextItemDO();
                updateObj.setId(existing.getId());
                updateObj.setModuleKey(item.getModuleKey());
                updateObj.setItemValue(item.getItemValue());
                updateObj.setSort(item.getSort());
                updateObj.setRemark(item.getRemark());
                textItemMapper.updateById(updateObj);
            } else {
                TextItemDO entity = new TextItemDO();
                entity.setProfileId(reqVO.getProfileId());
                entity.setPageKey(reqVO.getPageKey());
                entity.setModuleKey(item.getModuleKey());
                entity.setItemKey(item.getItemKey());
                entity.setItemValue(item.getItemValue());
                entity.setSort(item.getSort());
                entity.setRemark(item.getRemark());
                textItemMapper.insert(entity);
            }
        }
    }

}
