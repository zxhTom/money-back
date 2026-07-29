package cn.iocoder.yudao.module.custom.dal.mysql.invite;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.custom.dal.dataobject.invite.InviteCodeDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface InviteCodeMapper extends BaseMapperX<InviteCodeDO> {

    default InviteCodeDO selectByCode(String code) {
        return selectOne(new LambdaQueryWrapperX<InviteCodeDO>()
                .eq(InviteCodeDO::getCode, code));
    }

    default List<InviteCodeDO> selectListByInviter(Long inviterUserId) {
        return selectList(new LambdaQueryWrapperX<InviteCodeDO>()
                .eq(InviteCodeDO::getInviterUserId, inviterUserId)
                .orderByDesc(InviteCodeDO::getId));
    }

    /** 该用户当前状态为"有效"的邀请码（正常情况下同时最多一条，过期与否由调用方判断） */
    default InviteCodeDO selectActiveByInviter(Long inviterUserId) {
        return selectOne(new LambdaQueryWrapperX<InviteCodeDO>()
                .eq(InviteCodeDO::getInviterUserId, inviterUserId)
                .eq(InviteCodeDO::getStatus, InviteCodeDO.STATUS_ACTIVE));
    }

}
