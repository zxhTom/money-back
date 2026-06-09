package cn.iocoder.yudao.module.system.dal.mysql.monitor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.DataAccessLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.DataAccessLogDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DataAccessLogMapper extends BaseMapperX<DataAccessLogDO> {

    default PageResult<DataAccessLogDO> selectPage(DataAccessLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DataAccessLogDO>()
                .eqIfPresent(DataAccessLogDO::getUserId, reqVO.getUserId())
                .eqIfPresent(DataAccessLogDO::getModule, reqVO.getModule())
                .eqIfPresent(DataAccessLogDO::getEntityType, reqVO.getEntityType())
                .betweenIfPresent(DataAccessLogDO::getAccessTime, reqVO.getAccessTime())
                .eq(DataAccessLogDO::getDeleted, 0)
                .orderByDesc(DataAccessLogDO::getId));
    }
}
