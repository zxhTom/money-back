package cn.iocoder.yudao.module.custom.dal.mysql.skin;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.custom.controller.admin.skin.vo.SkinProfilePageReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.skin.SkinProfileDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 小程序皮肤配置 Mapper
 */
@Mapper
public interface SkinProfileMapper extends BaseMapperX<SkinProfileDO> {

    default PageResult<SkinProfileDO> selectPage(SkinProfilePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SkinProfileDO>()
                .likeIfPresent(SkinProfileDO::getName, reqVO.getName())
                .eqIfPresent(SkinProfileDO::getType, reqVO.getType())
                .orderByDesc(SkinProfileDO::getId));
    }

    default SkinProfileDO selectActive() {
        return selectOne(SkinProfileDO::getIsActive, true);
    }

    default SkinProfileDO selectByCode(String code) {
        return selectOne(SkinProfileDO::getCode, code);
    }

    /** 将当前生效的皮肤全部清空为未生效，供切换生效时使用 */
    default void clearActive() {
        update(null, new LambdaUpdateWrapper<SkinProfileDO>()
                .eq(SkinProfileDO::getIsActive, true)
                .set(SkinProfileDO::getIsActive, false));
    }

}
