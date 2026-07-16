package cn.iocoder.yudao.module.custom.dal.mysql.invite;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.custom.dal.dataobject.invite.InviteRegisterLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface InviteRegisterLogMapper extends BaseMapperX<InviteRegisterLogDO> {

    default List<InviteRegisterLogDO> selectListByCodeId(Long inviteCodeId) {
        return selectList(new LambdaQueryWrapperX<InviteRegisterLogDO>()
                .eq(InviteRegisterLogDO::getInviteCodeId, inviteCodeId)
                .orderByDesc(InviteRegisterLogDO::getId));
    }

}
