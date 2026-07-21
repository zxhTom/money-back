package cn.iocoder.yudao.module.custom.service.skin;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.custom.controller.admin.skin.vo.SkinCloneReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.skin.vo.SkinProfilePageReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.skin.vo.SkinProfileSaveReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.skin.SkinProfileDO;
import cn.iocoder.yudao.module.custom.dal.mysql.skin.SkinProfileMapper;
import cn.iocoder.yudao.module.custom.framework.skin.util.CssVarDeclarationValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.custom.enums.CustomErrorCodeConstants.SKIN_PROFILE_CANNOT_DELETE;
import static cn.iocoder.yudao.module.custom.enums.CustomErrorCodeConstants.SKIN_PROFILE_CSS_TEXT_INVALID;
import static cn.iocoder.yudao.module.custom.enums.CustomErrorCodeConstants.SKIN_PROFILE_NOT_EXISTS;
import static cn.iocoder.yudao.module.custom.enums.CustomErrorCodeConstants.SKIN_PROFILE_PRESET_CANNOT_MODIFY;

/**
 * 小程序皮肤配置 Service 实现类
 */
@Service
@Slf4j
public class SkinProfileServiceImpl implements SkinProfileService {

    @Resource
    private SkinProfileMapper skinProfileMapper;

    @Override
    public PageResult<SkinProfileDO> getSkinProfilePage(SkinProfilePageReqVO reqVO) {
        return skinProfileMapper.selectPage(reqVO);
    }

    @Override
    public SkinProfileDO getSkinProfile(Long id) {
        return skinProfileMapper.selectById(id);
    }

    @Override
    public Long createSkinProfile(SkinProfileSaveReqVO reqVO) {
        validateCssText(reqVO.getCustomCssText());
        SkinProfileDO entity = new SkinProfileDO();
        entity.setName(reqVO.getName());
        entity.setConfigMode(reqVO.getConfigMode());
        entity.setTokens(reqVO.getTokens());
        entity.setCustomCssText(reqVO.getCustomCssText());
        entity.setThumbnailUrl(reqVO.getThumbnailUrl());
        entity.setSort(reqVO.getSort());
        entity.setRemark(reqVO.getRemark());
        entity.setType(SkinProfileDO.TYPE_CUSTOM);
        entity.setIsActive(false);
        skinProfileMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void updateSkinProfile(SkinProfileSaveReqVO reqVO) {
        SkinProfileDO existing = validateSkinProfileExists(reqVO.getId());
        validateCssText(reqVO.getCustomCssText());
        boolean isPreset = SkinProfileDO.TYPE_PRESET == existing.getType();
        if (isPreset && coreFieldsChanged(existing, reqVO)) {
            throw exception(SKIN_PROFILE_PRESET_CANNOT_MODIFY);
        }

        SkinProfileDO updateObj = new SkinProfileDO();
        updateObj.setId(reqVO.getId());
        updateObj.setName(reqVO.getName());
        updateObj.setSort(reqVO.getSort());
        updateObj.setRemark(reqVO.getRemark());
        updateObj.setThumbnailUrl(reqVO.getThumbnailUrl());
        if (!isPreset) {
            updateObj.setConfigMode(reqVO.getConfigMode());
            updateObj.setTokens(reqVO.getTokens());
            updateObj.setCustomCssText(reqVO.getCustomCssText());
        }
        skinProfileMapper.updateById(updateObj);
    }

    @Override
    public void deleteSkinProfile(Long id) {
        SkinProfileDO existing = validateSkinProfileExists(id);
        if (SkinProfileDO.TYPE_PRESET == existing.getType() || Boolean.TRUE.equals(existing.getIsActive())) {
            throw exception(SKIN_PROFILE_CANNOT_DELETE);
        }
        skinProfileMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void useSkinProfile(Long id) {
        validateSkinProfileExists(id);
        skinProfileMapper.clearActive();
        SkinProfileDO updateObj = new SkinProfileDO();
        updateObj.setId(id);
        updateObj.setIsActive(true);
        skinProfileMapper.updateById(updateObj);
    }

    @Override
    public Long cloneAsCustom(SkinCloneReqVO reqVO) {
        SkinProfileDO source = validateSkinProfileExists(reqVO.getPresetId());
        SkinProfileDO entity = new SkinProfileDO();
        entity.setType(SkinProfileDO.TYPE_CUSTOM);
        entity.setSourcePresetId(reqVO.getPresetId());
        entity.setCode("custom-" + UUID.randomUUID().toString().replace("-", ""));
        entity.setName(reqVO.getName());
        entity.setConfigMode(source.getConfigMode());
        entity.setTokens(source.getTokens() != null ? new HashMap<>(source.getTokens()) : new HashMap<>());
        entity.setCustomCssText(source.getCustomCssText());
        entity.setIsActive(false);
        skinProfileMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public SkinProfileDO getActiveSkinProfile() {
        return skinProfileMapper.selectActive();
    }

    private SkinProfileDO validateSkinProfileExists(Long id) {
        SkinProfileDO existing = skinProfileMapper.selectById(id);
        if (existing == null) {
            throw exception(SKIN_PROFILE_NOT_EXISTS);
        }
        return existing;
    }

    private void validateCssText(String cssText) {
        if (!CssVarDeclarationValidator.isValid(cssText)) {
            throw exception(SKIN_PROFILE_CSS_TEXT_INVALID);
        }
    }

    private boolean coreFieldsChanged(SkinProfileDO existing, SkinProfileSaveReqVO reqVO) {
        return !Objects.equals(existing.getConfigMode(), reqVO.getConfigMode())
                || !Objects.equals(existing.getTokens(), reqVO.getTokens())
                || !Objects.equals(existing.getCustomCssText(), reqVO.getCustomCssText());
    }

}
