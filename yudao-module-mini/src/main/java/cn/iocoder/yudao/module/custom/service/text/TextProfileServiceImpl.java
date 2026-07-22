package cn.iocoder.yudao.module.custom.service.text;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.custom.controller.admin.text.vo.TextProfilePageReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.text.vo.TextProfileSaveReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.text.TextItemDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.text.TextProfileDO;
import cn.iocoder.yudao.module.custom.dal.mysql.text.TextItemMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.text.TextProfileMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.custom.enums.CustomErrorCodeConstants.TEXT_PROFILE_ACTIVE_CANNOT_DELETE;
import static cn.iocoder.yudao.module.custom.enums.CustomErrorCodeConstants.TEXT_PROFILE_NOT_EXISTS;

/**
 * 小程序文案配置 Service 实现类
 */
@Service
public class TextProfileServiceImpl implements TextProfileService {

    @Resource
    private TextProfileMapper textProfileMapper;

    @Resource
    private TextItemMapper textItemMapper;

    @Override
    public PageResult<TextProfileDO> getTextProfilePage(TextProfilePageReqVO reqVO) {
        return textProfileMapper.selectPage(reqVO);
    }

    @Override
    public TextProfileDO getTextProfile(Long id) {
        return textProfileMapper.selectById(id);
    }

    @Override
    public Long createTextProfile(TextProfileSaveReqVO reqVO) {
        TextProfileDO entity = new TextProfileDO();
        entity.setName(reqVO.getName());
        entity.setRemark(reqVO.getRemark());
        entity.setCode(generateCode());
        entity.setSeedFrom("safe");
        entity.setTextMode(reqVO.getTextMode());
        entity.setIsActive(false);
        textProfileMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void updateTextProfile(TextProfileSaveReqVO reqVO) {
        validateTextProfileExists(reqVO.getId());
        TextProfileDO updateObj = new TextProfileDO();
        updateObj.setId(reqVO.getId());
        updateObj.setName(reqVO.getName());
        updateObj.setRemark(reqVO.getRemark());
        textProfileMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTextProfile(Long id) {
        TextProfileDO existing = validateTextProfileExists(id);
        if (Boolean.TRUE.equals(existing.getIsActive())) {
            throw exception(TEXT_PROFILE_ACTIVE_CANNOT_DELETE);
        }
        textItemMapper.deleteByProfileId(id);
        textProfileMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void useTextProfile(Long id) {
        TextProfileDO existing = validateTextProfileExists(id);
        textProfileMapper.clearActive(existing.getTextMode());
        TextProfileDO updateObj = new TextProfileDO();
        updateObj.setId(id);
        updateObj.setIsActive(true);
        textProfileMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long cloneProfile(Long sourceId, String newName) {
        TextProfileDO source = validateTextProfileExists(sourceId);

        TextProfileDO entity = new TextProfileDO();
        entity.setName(newName);
        entity.setCode(generateCode());
        entity.setSeedFrom(source.getCode());
        entity.setTextMode(source.getTextMode());
        entity.setIsActive(false);
        textProfileMapper.insert(entity);

        List<TextItemDO> sourceItems = textItemMapper.selectListByProfileId(sourceId);
        if (!sourceItems.isEmpty()) {
            List<TextItemDO> copies = sourceItems.stream().map(src -> {
                TextItemDO copy = new TextItemDO();
                copy.setProfileId(entity.getId());
                copy.setPageKey(src.getPageKey());
                copy.setModuleKey(src.getModuleKey());
                copy.setItemKey(src.getItemKey());
                copy.setItemValue(src.getItemValue());
                copy.setSort(src.getSort());
                copy.setRemark(src.getRemark());
                return copy;
            }).collect(Collectors.toList());
            textItemMapper.insertBatch(copies);
        }
        return entity.getId();
    }

    @Override
    public TextProfileDO getActiveTextProfile(String textMode) {
        return textProfileMapper.selectActive(textMode);
    }

    private TextProfileDO validateTextProfileExists(Long id) {
        TextProfileDO existing = textProfileMapper.selectById(id);
        if (existing == null) {
            throw exception(TEXT_PROFILE_NOT_EXISTS);
        }
        return existing;
    }

    private static String generateCode() {
        return "text-" + UUID.randomUUID().toString().replace("-", "");
    }

}
