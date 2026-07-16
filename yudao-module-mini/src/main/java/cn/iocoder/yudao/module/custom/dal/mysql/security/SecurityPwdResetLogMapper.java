package cn.iocoder.yudao.module.custom.dal.mysql.security;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.custom.dal.dataobject.security.SecurityPwdResetLogDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SecurityPwdResetLogMapper extends BaseMapperX<SecurityPwdResetLogDO> {

    default PageResult<SecurityPwdResetLogDO> selectPage(PageParam page, Long userId) {
        return selectPage(page, new LambdaQueryWrapperX<SecurityPwdResetLogDO>()
                .eqIfPresent(SecurityPwdResetLogDO::getUserId, userId)
                .orderByDesc(SecurityPwdResetLogDO::getId));
    }

}
