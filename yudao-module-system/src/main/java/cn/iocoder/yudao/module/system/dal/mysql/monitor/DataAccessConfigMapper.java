package cn.iocoder.yudao.module.system.dal.mysql.monitor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.DataAccessConfigPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.DataAccessConfigDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DataAccessConfigMapper extends BaseMapperX<DataAccessConfigDO> {

    default PageResult<DataAccessConfigDO> selectPage(DataAccessConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DataAccessConfigDO>()
                .eqIfPresent(DataAccessConfigDO::getModule, reqVO.getModule())
                .eqIfPresent(DataAccessConfigDO::getEntityType, reqVO.getEntityType())
                .eqIfPresent(DataAccessConfigDO::getUserId, reqVO.getUserId())
                .eqIfPresent(DataAccessConfigDO::getEnabled, reqVO.getEnabled())
                .eq(DataAccessConfigDO::getDeleted, 0)
                .orderByDesc(DataAccessConfigDO::getId));
    }

    @Select("SELECT * FROM custom_data_access_config WHERE module = #{module} AND entity_type = #{entityType} AND deleted = 0")
    List<DataAccessConfigDO> selectByModuleAndEntityType(String module, String entityType);
}
