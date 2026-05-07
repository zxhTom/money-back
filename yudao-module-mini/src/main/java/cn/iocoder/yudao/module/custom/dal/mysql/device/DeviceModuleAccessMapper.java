package cn.iocoder.yudao.module.custom.dal.mysql.device;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.custom.controller.admin.device.vo.DeviceModuleAccessPageReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.device.DeviceModuleAccessDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DeviceModuleAccessMapper extends BaseMapperX<DeviceModuleAccessDO> {

    default DeviceModuleAccessDO selectByModuleAndDevice(String moduleName, String deviceId) {
        return selectOne(DeviceModuleAccessDO::getModuleName, moduleName,
                DeviceModuleAccessDO::getDeviceId, deviceId);
    }

    /**
     * 查询与 moduleName + deviceId 相同的记录，排除指定 id（用于更新时唯一性校验）
     */
    default DeviceModuleAccessDO selectByModuleAndDeviceExcludeId(String moduleName, String deviceId, Long excludeId) {
        return selectOne(new LambdaQueryWrapperX<DeviceModuleAccessDO>()
                .eq(DeviceModuleAccessDO::getModuleName, moduleName)
                .eq(DeviceModuleAccessDO::getDeviceId, deviceId)
                .ne(excludeId != null, DeviceModuleAccessDO::getId, excludeId));
    }

    default PageResult<DeviceModuleAccessDO> selectPage(DeviceModuleAccessPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DeviceModuleAccessDO>()
                .likeIfPresent(DeviceModuleAccessDO::getModuleName, reqVO.getModuleName())
                .likeIfPresent(DeviceModuleAccessDO::getDeviceId, reqVO.getDeviceId())
                .eqIfPresent(DeviceModuleAccessDO::getEnabled, reqVO.getEnabled())
                .orderByDesc(DeviceModuleAccessDO::getId));
    }

    default List<DeviceModuleAccessDO> selectListByModuleName(String moduleName) {
        return selectList(DeviceModuleAccessDO::getModuleName, moduleName);
    }

}

