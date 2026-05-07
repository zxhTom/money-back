package cn.iocoder.yudao.module.custom.service.device;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.custom.controller.admin.device.vo.DeviceModuleAccessPageReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.device.vo.DeviceModuleAccessSetReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.device.vo.DeviceModuleAccessUpdateReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.device.DeviceModuleAccessDO;
import cn.iocoder.yudao.module.custom.dal.mysql.device.DeviceModuleAccessMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Comparator;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.custom.enums.CustomErrorCodeConstants.DEVICE_MODULE_ACCESS_MODULE_DEVICE_DUPLICATE;
import static cn.iocoder.yudao.module.custom.enums.CustomErrorCodeConstants.DEVICE_MODULE_ACCESS_NOT_EXISTS;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Service
@Validated
@Slf4j
public class DeviceModuleAccessServiceImpl implements DeviceModuleAccessService {

    @Resource
    private DeviceModuleAccessMapper deviceModuleAccessMapper;

    @Override
    public void setAccess(DeviceModuleAccessSetReqVO reqVO) {
        String moduleName = StrUtil.trim(reqVO.getModuleName());
        String deviceId = StrUtil.trim(reqVO.getDeviceId());
        DeviceModuleAccessDO exists = deviceModuleAccessMapper.selectByModuleAndDevice(moduleName, deviceId);
        if (exists == null) {
            DeviceModuleAccessDO entity = DeviceModuleAccessDO.builder()
                    .moduleName(moduleName)
                    .deviceId(deviceId)
                    .enabled(Boolean.TRUE.equals(reqVO.getEnabled()))
                    .build();
            deviceModuleAccessMapper.insert(entity);
            return;
        }
        DeviceModuleAccessDO updateObj = new DeviceModuleAccessDO();
        updateObj.setId(exists.getId());
        updateObj.setEnabled(Boolean.TRUE.equals(reqVO.getEnabled()));
        deviceModuleAccessMapper.updateById(updateObj);
    }

    @Override
    public void updateAccess(DeviceModuleAccessUpdateReqVO reqVO) {
        Long id = reqVO.getId();
        DeviceModuleAccessDO row = deviceModuleAccessMapper.selectById(id);
        if (row == null) {
            throw exception(DEVICE_MODULE_ACCESS_NOT_EXISTS);
        }
        String moduleName = StrUtil.trim(reqVO.getModuleName());
        String deviceId = StrUtil.trim(reqVO.getDeviceId());
        DeviceModuleAccessDO conflict = deviceModuleAccessMapper.selectByModuleAndDeviceExcludeId(moduleName, deviceId, id);
        if (conflict != null) {
            throw exception(DEVICE_MODULE_ACCESS_MODULE_DEVICE_DUPLICATE);
        }
        DeviceModuleAccessDO updateObj = new DeviceModuleAccessDO();
        updateObj.setId(id);
        updateObj.setModuleName(moduleName);
        updateObj.setDeviceId(deviceId);
        updateObj.setEnabled(Boolean.TRUE.equals(reqVO.getEnabled()));
        deviceModuleAccessMapper.updateById(updateObj);
    }

    @Override
    public boolean isEnabled(String moduleName, String deviceId) {
        String module = StrUtil.trim(moduleName);
        String device = StrUtil.trim(deviceId);
        if (StrUtil.isBlank(module) || StrUtil.isBlank(device)) {
            return false;
        }

        // 1) 精确匹配优先
        DeviceModuleAccessDO exact = deviceModuleAccessMapper.selectByModuleAndDevice(module, device);
        if (exact != null) {
            return Boolean.TRUE.equals(exact.getEnabled());
        }

        // 2) 其次匹配正则（例如 .* 表示所有设备）
        List<DeviceModuleAccessDO> list = deviceModuleAccessMapper.selectListByModuleName(module);
        DeviceModuleAccessDO matched = list.stream()
                .filter(r -> regexMatches(r.getDeviceId(), device))
                .max(Comparator
                        .comparing(DeviceModuleAccessDO::getUpdateTime, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(DeviceModuleAccessDO::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
        return matched != null && Boolean.TRUE.equals(matched.getEnabled());
    }

    @Override
    public DeviceModuleAccessDO getByModuleAndDevice(String moduleName, String deviceId) {
        return deviceModuleAccessMapper.selectByModuleAndDevice(StrUtil.trim(moduleName), StrUtil.trim(deviceId));
    }

    @Override
    public PageResult<DeviceModuleAccessDO> getPage(DeviceModuleAccessPageReqVO reqVO) {
        return deviceModuleAccessMapper.selectPage(reqVO);
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            return;
        }
        deviceModuleAccessMapper.deleteById(id);
    }

    @Override
    public DeviceModuleAccessDO getById(Long id) {
        if (id == null) {
            return null;
        }
        return deviceModuleAccessMapper.selectById(id);
    }

    private boolean regexMatches(String regex, String deviceId) {
        if (StrUtil.isBlank(regex) || StrUtil.isBlank(deviceId)) {
            return false;
        }
        try {
            return Pattern.matches(regex.trim(), deviceId);
        } catch (PatternSyntaxException ex) {
            log.warn("[device-module-access][invalid-device-regex][regex={}]", regex);
            return false;
        }
    }
}

