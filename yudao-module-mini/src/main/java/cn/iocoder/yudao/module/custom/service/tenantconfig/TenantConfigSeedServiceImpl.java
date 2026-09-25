package cn.iocoder.yudao.module.custom.service.tenantconfig;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.tenant.config.TenantProperties;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.custom.dal.dataobject.changelog.VersionChangelogDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractModelDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.iconset.IconSetProfileDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.miniconfig.MiniProgramConfigDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.skin.SkinProfileDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.speedcontrol.SpeedControlConfigDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.text.TextItemDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.text.TextProfileDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.timewindow.TimeWindowDO;
import cn.iocoder.yudao.module.custom.dal.mysql.changelog.VersionChangelogMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.contract.ContractModelMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.iconset.IconSetProfileMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.miniconfig.MiniProgramConfigMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.skin.SkinProfileMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.speedcontrol.SpeedControlConfigMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.text.TextItemMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.text.TextProfileMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.timewindow.TimeWindowMapper;
import cn.iocoder.yudao.module.fee.dal.dataobject.strategy.StrategyDO;
import cn.iocoder.yudao.module.fee.dal.mysql.strategy.StrategyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0;

@Service
@Slf4j
public class TenantConfigSeedServiceImpl implements TenantConfigSeedService {

    @Autowired(required = false)
    private TenantProperties tenantProperties;

    @Resource
    private SkinProfileMapper skinProfileMapper;
    @Resource
    private TextProfileMapper textProfileMapper;
    @Resource
    private TextItemMapper textItemMapper;
    @Resource
    private IconSetProfileMapper iconSetProfileMapper;
    @Resource
    private MiniProgramConfigMapper miniProgramConfigMapper;
    @Resource
    private VersionChangelogMapper versionChangelogMapper;
    @Resource
    private SpeedControlConfigMapper speedControlConfigMapper;
    @Resource
    private StrategyMapper strategyMapper;
    @Resource
    private ContractModelMapper contractModelMapper;
    @Resource
    private TimeWindowMapper timeWindowMapper;

    @Override
    public Map<String, Integer> seedTenantConfig(Long targetTenantId) {
        if (tenantProperties == null) {
            throw exception0(400, "请先开启多租户后再初始化配置");
        }
        Long sourceTenantId = tenantProperties.getDefaultTenantId() != null ? tenantProperties.getDefaultTenantId() : 1L;
        if (sourceTenantId.equals(targetTenantId)) {
            throw exception0(400, "目标租户不能与源租户相同");
        }

        Map<String, Integer> result = new LinkedHashMap<>();

        // 1. custom_skin_profile
        result.put("custom_skin_profile", copyTable(sourceTenantId, targetTenantId, skinProfileMapper, SkinProfileDO.class));

        // 2. custom_text_profile & custom_text_item
        Map<Long, Long> textProfileIdMap = new HashMap<>();
        int textProfileCount = copyTextProfile(sourceTenantId, targetTenantId, textProfileIdMap);
        result.put("custom_text_profile", textProfileCount);

        int textItemCount = copyTextItem(sourceTenantId, targetTenantId, textProfileIdMap, textProfileCount == -1);
        result.put("custom_text_item", textItemCount);

        // 3. custom_icon_set_profile
        result.put("custom_icon_set_profile", copyTable(sourceTenantId, targetTenantId, iconSetProfileMapper, IconSetProfileDO.class));

        // 4. custom_miniprogram_config
        result.put("custom_miniprogram_config", copyTable(sourceTenantId, targetTenantId, miniProgramConfigMapper, MiniProgramConfigDO.class));

        // 5. custom_version_changelog
        result.put("custom_version_changelog", copyTable(sourceTenantId, targetTenantId, versionChangelogMapper, VersionChangelogDO.class));

        // 6. custom_speed_control_config
        result.put("custom_speed_control_config", copyTable(sourceTenantId, targetTenantId, speedControlConfigMapper, SpeedControlConfigDO.class));

        // 7. fee_strategy
        result.put("fee_strategy", copyTable(sourceTenantId, targetTenantId, strategyMapper, StrategyDO.class));

        // 8. contract_model
        result.put("contract_model", copyTable(sourceTenantId, targetTenantId, contractModelMapper, ContractModelDO.class));

        // 9. time_window
        result.put("time_window", copyTable(sourceTenantId, targetTenantId, timeWindowMapper, TimeWindowDO.class));

        return result;
    }

    private <T> int copyTable(Long sourceTenantId, Long targetTenantId,
                              BaseMapperX<T> mapper,
                              Class<T> clazz) {
        Boolean targetHasData = TenantUtils.execute(targetTenantId, () -> mapper.selectCount() > 0);
        if (Boolean.TRUE.equals(targetHasData)) {
            return -1;
        }
        List<T> sourceList = TenantUtils.execute(sourceTenantId, () -> mapper.selectList());
        if (sourceList == null || sourceList.isEmpty()) {
            return 0;
        }
        TenantUtils.execute(targetTenantId, () -> {
            for (T item : sourceList) {
                T newObj = BeanUtils.toBean(item, clazz);
                resetTenantDOFields(newObj);
                mapper.insert(newObj);
            }
        });
        return sourceList.size();
    }

    private int copyTextProfile(Long sourceTenantId, Long targetTenantId, Map<Long, Long> profileIdMap) {
        Boolean targetHasData = TenantUtils.execute(targetTenantId, () -> textProfileMapper.selectCount() > 0);
        if (Boolean.TRUE.equals(targetHasData)) {
            return -1;
        }
        List<TextProfileDO> sourceList = TenantUtils.execute(sourceTenantId, () -> textProfileMapper.selectList());
        if (sourceList == null || sourceList.isEmpty()) {
            return 0;
        }
        TenantUtils.execute(targetTenantId, () -> {
            for (TextProfileDO profile : sourceList) {
                Long oldId = profile.getId();
                TextProfileDO newProfile = BeanUtils.toBean(profile, TextProfileDO.class);
                resetTenantDOFields(newProfile);
                textProfileMapper.insert(newProfile);
                profileIdMap.put(oldId, newProfile.getId());
            }
        });
        return sourceList.size();
    }

    private int copyTextItem(Long sourceTenantId, Long targetTenantId, Map<Long, Long> profileIdMap, boolean profileSkipped) {
        Boolean targetHasData = TenantUtils.execute(targetTenantId, () -> textItemMapper.selectCount() > 0);
        if (Boolean.TRUE.equals(targetHasData) || profileSkipped) {
            return -1;
        }
        List<TextItemDO> sourceList = TenantUtils.execute(sourceTenantId, () -> textItemMapper.selectList());
        if (sourceList == null || sourceList.isEmpty()) {
            return 0;
        }
        TenantUtils.execute(targetTenantId, () -> {
            for (TextItemDO item : sourceList) {
                TextItemDO newItem = BeanUtils.toBean(item, TextItemDO.class);
                resetTenantDOFields(newItem);
                Long newProfileId = profileIdMap.get(item.getProfileId());
                if (newProfileId != null) {
                    newItem.setProfileId(newProfileId);
                }
                textItemMapper.insert(newItem);
            }
        });
        return sourceList.size();
    }

    private void resetTenantDOFields(Object obj) {
        boolean idReset = false;
        Class<?> clazz = obj.getClass();
        while (clazz != null && clazz != Object.class) {
            try {
                Field idField = clazz.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(obj, null);
                idReset = true;
                break;
            } catch (NoSuchFieldException ignored) {
                clazz = clazz.getSuperclass();
            } catch (Exception e) {
                throw exception0(500, "重置实体 ID 字段失败: " + obj.getClass().getName());
            }
        }
        if (!idReset) {
            throw exception0(500, "未找到 id 字段: " + obj.getClass().getName());
        }

        if (obj instanceof TenantBaseDO) {
            TenantBaseDO tenantDO = (TenantBaseDO) obj;
            tenantDO.setTenantId(null);
            tenantDO.setCreateTime(null);
            tenantDO.setUpdateTime(null);
            tenantDO.setCreator(null);
            tenantDO.setUpdater(null);
        }
    }
}
