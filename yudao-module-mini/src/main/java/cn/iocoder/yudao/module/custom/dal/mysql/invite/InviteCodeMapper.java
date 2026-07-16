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

}
