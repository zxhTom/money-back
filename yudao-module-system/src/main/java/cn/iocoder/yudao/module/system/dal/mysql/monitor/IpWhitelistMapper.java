package cn.iocoder.yudao.module.system.dal.mysql.monitor;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.IpWhitelistDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IpWhitelistMapper extends BaseMapperX<IpWhitelistDO> {

    default List<IpWhitelistDO> selectEnabledList() {
        return selectList(new LambdaQueryWrapperX<IpWhitelistDO>()
                .eq(IpWhitelistDO::getEnabled, 1));
    }

    default List<IpWhitelistDO> selectListBySource(String source) {
        return selectList(new LambdaQueryWrapperX<IpWhitelistDO>()
                .eq(IpWhitelistDO::getSource, source));
    }

    default PageResult<IpWhitelistDO> selectPage(PageParam page) {
        return selectPage(page, new LambdaQueryWrapperX<IpWhitelistDO>()
                .ne(IpWhitelistDO::getIp, "__INTERNAL_ALLOW__") // 隐藏内网放行开关哨兵行
                .orderByDesc(IpWhitelistDO::getId));
    }
}
