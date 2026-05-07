package cn.iocoder.yudao.module.custom.service.device;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.custom.controller.admin.device.vo.DeviceModuleAccessPageReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.device.vo.DeviceModuleAccessSetReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.device.vo.DeviceModuleAccessUpdateReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.device.DeviceModuleAccessDO;

import javax.validation.Valid;

public interface DeviceModuleAccessService {

    /**
     * 按模块 + 设备设置开放状态（存在则更新，不存在则新增）
     */
    void setAccess(@Valid DeviceModuleAccessSetReqVO reqVO);

    /**
     * 按主键更新模块名、设备 ID、开关（弹窗编辑；与 {@link #setAccess} 幂等按 module+device 区分）
     */
    void updateAccess(@Valid DeviceModuleAccessUpdateReqVO reqVO);

    /**
     * 查询模块在设备上的开放状态（不存在默认 false）
     */
    boolean isEnabled(String moduleName, String deviceId);

    /**
     * 查询规则原始记录
     */
    DeviceModuleAccessDO getByModuleAndDevice(String moduleName, String deviceId);

    /**
     * 分页查询开放规则
     */
    PageResult<DeviceModuleAccessDO> getPage(DeviceModuleAccessPageReqVO reqVO);

    /**
     * 删除配置
     */
    void deleteById(Long id);

    /**
     * 按主键查询（编辑前拉最新数据等场景）
     */
    DeviceModuleAccessDO getById(Long id);
}

