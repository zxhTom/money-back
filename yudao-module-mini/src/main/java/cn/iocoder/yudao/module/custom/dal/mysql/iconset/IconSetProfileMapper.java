package cn.iocoder.yudao.module.custom.dal.mysql.iconset;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.custom.controller.admin.iconset.vo.IconSetProfilePageReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.iconset.IconSetProfileDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 小程序图标集配置 Mapper
 */
@Mapper
public interface IconSetProfileMapper extends BaseMapperX<IconSetProfileDO> {

    default PageResult<IconSetProfileDO> selectPage(IconSetProfilePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IconSetProfileDO>()
                .likeIfPresent(IconSetProfileDO::getName, reqVO.getName())
                .eqIfPresent(IconSetProfileDO::getType, reqVO.getType())
                .orderByDesc(IconSetProfileDO::getId));
    }

    default IconSetProfileDO selectActive() {
        return selectOne(IconSetProfileDO::getIsActive, true);
    }

    default IconSetProfileDO selectByCode(String code) {
        return selectOne(IconSetProfileDO::getCode, code);
    }

    /** 将当前生效的图标集全部清空为未生效，供切换生效时使用 */
    default void clearActive() {
        update(null, new LambdaUpdateWrapper<IconSetProfileDO>()
                .eq(IconSetProfileDO::getIsActive, true)
                .set(IconSetProfileDO::getIsActive, false));
    }

}
