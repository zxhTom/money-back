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

    default TextProfileDO selectActive() {
        return selectOne(TextProfileDO::getIsActive, true);
    }

    default TextProfileDO selectByCode(String code) {
        return selectOne(TextProfileDO::getCode, code);
    }

    /** 将当前生效的文案套全部清空为未生效，供切换生效时使用 */
    default void clearActive() {
        update(null, new LambdaUpdateWrapper<TextProfileDO>()
                .eq(TextProfileDO::getIsActive, true)
                .set(TextProfileDO::getIsActive, false));
    }

}
