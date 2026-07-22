package cn.iocoder.yudao.module.custom.dal.mysql.text;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.custom.controller.admin.text.vo.TextProfilePageReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.text.TextProfileDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 小程序文案配置 Mapper
 */
@Mapper
public interface TextProfileMapper extends BaseMapperX<TextProfileDO> {

    default PageResult<TextProfileDO> selectPage(TextProfilePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TextProfileDO>()
                .likeIfPresent(TextProfileDO::getName, reqVO.getName())
                .orderByDesc(TextProfileDO::getId));
    }

    /** 获取指定 textMode 内当前生效的文案套 */
    default TextProfileDO selectActive(String textMode) {
        return selectOne(new LambdaQueryWrapperX<TextProfileDO>()
                .eq(TextProfileDO::getIsActive, true)
                .eq(TextProfileDO::getTextMode, textMode));
    }

    default TextProfileDO selectByCode(String code) {
        return selectOne(TextProfileDO::getCode, code);
    }

    /** 将指定 textMode 内当前生效的文案套清空为未生效（不影响其他 textMode），供切换生效时使用 */
    default void clearActive(String textMode) {
        update(null, new LambdaUpdateWrapper<TextProfileDO>()
                .eq(TextProfileDO::getIsActive, true)
                .eq(TextProfileDO::getTextMode, textMode)
                .set(TextProfileDO::getIsActive, false));
    }

}
