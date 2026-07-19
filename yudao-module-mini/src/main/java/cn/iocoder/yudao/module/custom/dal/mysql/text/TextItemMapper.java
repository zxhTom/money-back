package cn.iocoder.yudao.module.custom.dal.mysql.text;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.custom.dal.dataobject.text.TextItemDO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 小程序文案条目 Mapper
 */
@Mapper
public interface TextItemMapper extends BaseMapperX<TextItemDO> {

    default List<TextItemDO> selectListByProfileId(Long profileId) {
        return selectList(TextItemDO::getProfileId, profileId);
    }

    default List<TextItemDO> selectListByProfileIdAndPageKey(Long profileId, String pageKey) {
        return selectList(new LambdaQueryWrapperX<TextItemDO>()
                .eq(TextItemDO::getProfileId, profileId)
                .eq(TextItemDO::getPageKey, pageKey)
                .orderByAsc(TextItemDO::getSort));
    }

    default List<TextItemDO> selectListByProfileIdAndKeyword(Long profileId, String keyword) {
        return selectList(new LambdaQueryWrapperX<TextItemDO>()
                .eq(TextItemDO::getProfileId, profileId)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(TextItemDO::getItemKey, keyword)
                        .or()
                        .like(TextItemDO::getItemValue, keyword))
                .orderByAsc(TextItemDO::getSort));
    }

    default void deleteByProfileId(Long profileId) {
        delete(TextItemDO::getProfileId, profileId);
    }

    default TextItemDO selectByProfileIdAndItemKey(Long profileId, String itemKey) {
        return selectOne(new LambdaQueryWrapperX<TextItemDO>()
                .eq(TextItemDO::getProfileId, profileId)
                .eq(TextItemDO::getItemKey, itemKey));
    }

}
