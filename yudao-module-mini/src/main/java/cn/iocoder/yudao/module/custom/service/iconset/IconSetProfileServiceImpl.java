package cn.iocoder.yudao.module.custom.service.iconset;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.custom.controller.admin.iconset.vo.IconSetCloneReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.iconset.vo.IconSetProfilePageReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.iconset.vo.IconSetProfileSaveReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.iconset.IconSetProfileDO;
import cn.iocoder.yudao.module.custom.dal.mysql.iconset.IconSetProfileMapper;
import cn.iocoder.yudao.module.custom.framework.iconset.util.SvgIconValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.custom.enums.CustomErrorCodeConstants.ICON_SET_PROFILE_CANNOT_DELETE;
import static cn.iocoder.yudao.module.custom.enums.CustomErrorCodeConstants.ICON_SET_PROFILE_NOT_EXISTS;
import static cn.iocoder.yudao.module.custom.enums.CustomErrorCodeConstants.ICON_SET_PROFILE_PRESET_CANNOT_MODIFY;
import static cn.iocoder.yudao.module.custom.enums.CustomErrorCodeConstants.ICON_SET_PROFILE_SVG_INVALID;

/**
 * 小程序图标集配置 Service 实现类
 */
@Service
@Slf4j
public class IconSetProfileServiceImpl implements IconSetProfileService {

    @Resource
    private IconSetProfileMapper iconSetProfileMapper;

    @Override
    public PageResult<IconSetProfileDO> getIconSetProfilePage(IconSetProfilePageReqVO reqVO) {
        return iconSetProfileMapper.selectPage(reqVO);
    }

    @Override
    public IconSetProfileDO getIconSetProfile(Long id) {
        return iconSetProfileMapper.selectById(id);
    }

    @Override
    public Long createIconSetProfile(IconSetProfileSaveReqVO reqVO) {
        validateIcons(reqVO.getIcons());
        IconSetProfileDO entity = new IconSetProfileDO();
        entity.setName(reqVO.getName());
        entity.setIcons(reqVO.getIcons());
        entity.setThumbnailUrl(reqVO.getThumbnailUrl());
        entity.setSort(reqVO.getSort());
        entity.setRemark(reqVO.getRemark());
        entity.setType(IconSetProfileDO.TYPE_CUSTOM);
        entity.setIsActive(false);
        iconSetProfileMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void updateIconSetProfile(IconSetProfileSaveReqVO reqVO) {
        IconSetProfileDO existing = validateIconSetProfileExists(reqVO.getId());
        validateIcons(reqVO.getIcons());
        boolean isPreset = IconSetProfileDO.TYPE_PRESET == existing.getType();
        if (isPreset && coreFieldsChanged(existing, reqVO)) {
            throw exception(ICON_SET_PROFILE_PRESET_CANNOT_MODIFY);
        }

        IconSetProfileDO updateObj = new IconSetProfileDO();
        updateObj.setId(reqVO.getId());
        updateObj.setName(reqVO.getName());
        updateObj.setSort(reqVO.getSort());
        updateObj.setRemark(reqVO.getRemark());
        updateObj.setThumbnailUrl(reqVO.getThumbnailUrl());
        if (!isPreset) {
            updateObj.setIcons(reqVO.getIcons());
        }
        iconSetProfileMapper.updateById(updateObj);
    }

    @Override
    public void deleteIconSetProfile(Long id) {
        IconSetProfileDO existing = validateIconSetProfileExists(id);
        if (IconSetProfileDO.TYPE_PRESET == existing.getType() || Boolean.TRUE.equals(existing.getIsActive())) {
            throw exception(ICON_SET_PROFILE_CANNOT_DELETE);
        }
        iconSetProfileMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void useIconSetProfile(Long id) {
        validateIconSetProfileExists(id);
        iconSetProfileMapper.clearActive();
        IconSetProfileDO updateObj = new IconSetProfileDO();
        updateObj.setId(id);
        updateObj.setIsActive(true);
        iconSetProfileMapper.updateById(updateObj);
    }

    @Override
    public Long cloneAsCustom(IconSetCloneReqVO reqVO) {
        IconSetProfileDO source = validateIconSetProfileExists(reqVO.getPresetId());
        IconSetProfileDO entity = new IconSetProfileDO();
        entity.setType(IconSetProfileDO.TYPE_CUSTOM);
        entity.setSourcePresetId(reqVO.getPresetId());
        entity.setCode("custom-" + UUID.randomUUID().toString().replace("-", ""));
        entity.setName(reqVO.getName());
        entity.setIcons(source.getIcons() != null ? new HashMap<>(source.getIcons()) : new HashMap<>());
        entity.setIsActive(false);
        iconSetProfileMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public IconSetProfileDO getActiveIconSetProfile() {
        return iconSetProfileMapper.selectActive();
    }

    private IconSetProfileDO validateIconSetProfileExists(Long id) {
        IconSetProfileDO existing = iconSetProfileMapper.selectById(id);
        if (existing == null) {
            throw exception(ICON_SET_PROFILE_NOT_EXISTS);
        }
        return existing;
    }

    private void validateIcons(Map<String, String> icons) {
        if (icons == null) {
            return;
        }
        for (String svg : icons.values()) {
            if (!SvgIconValidator.isValid(svg)) {
                throw exception(ICON_SET_PROFILE_SVG_INVALID);
            }
        }
    }

    private boolean coreFieldsChanged(IconSetProfileDO existing, IconSetProfileSaveReqVO reqVO) {
        return !Objects.equals(existing.getIcons(), reqVO.getIcons());
    }

}
