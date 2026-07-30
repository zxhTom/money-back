package cn.iocoder.yudao.module.custom.dal.mysql.changelog;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.custom.dal.dataobject.changelog.VersionChangelogDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface VersionChangelogMapper extends BaseMapperX<VersionChangelogDO> {

    default VersionChangelogDO selectByVersion(String version) {
        return selectOne(new LambdaQueryWrapperX<VersionChangelogDO>()
                .eq(VersionChangelogDO::getVersion, version));
    }

    default List<VersionChangelogDO> selectAllOrderByCreateTimeDesc() {
        return selectList(new LambdaQueryWrapperX<VersionChangelogDO>()
                .orderByDesc(VersionChangelogDO::getCreateTime));
    }

}
