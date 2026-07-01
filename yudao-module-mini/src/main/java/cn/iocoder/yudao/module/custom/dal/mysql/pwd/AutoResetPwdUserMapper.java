package cn.iocoder.yudao.module.custom.dal.mysql.pwd;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.custom.dal.dataobject.pwd.AutoResetPwdUserDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AutoResetPwdUserMapper extends BaseMapperX<AutoResetPwdUserDO> {

    default AutoResetPwdUserDO selectByUserId(Long userId) {
        return selectOne(new LambdaQueryWrapperX<AutoResetPwdUserDO>()
                .eq(AutoResetPwdUserDO::getUserId, userId));
    }

    default List<AutoResetPwdUserDO> selectAll() {
        return selectList(new LambdaQueryWrapperX<AutoResetPwdUserDO>());
    }

    default void deleteByUserId(Long userId) {
        delete(new LambdaQueryWrapperX<AutoResetPwdUserDO>()
                .eq(AutoResetPwdUserDO::getUserId, userId));
    }

}
