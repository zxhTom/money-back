package cn.iocoder.yudao.module.custom.service.speedcontrol;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.custom.controller.admin.speedcontrol.vo.SpeedControlConfigRespVO;
import cn.iocoder.yudao.module.custom.controller.admin.speedcontrol.vo.SpeedControlConfigSaveReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.speedcontrol.SpeedControlConfigDO;
import cn.iocoder.yudao.module.custom.dal.mysql.speedcontrol.SpeedControlConfigMapper;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SpeedControlConfigServiceImpl implements SpeedControlConfigService {

    private static final long CACHE_TTL_MS = 10_000L;

    @Resource
    private SpeedControlConfigMapper speedControlConfigMapper;
    @Resource
    private PermissionService permissionService;

    private static class CacheEntry {
        SpeedControlConfigDO config;
        Set<Long> exemptUserIds = Collections.emptySet();
        Set<Long> exemptRoleIds = Collections.emptySet();
        long cachedAt;
    }

    private final Map<Long, CacheEntry> tenantCache = new ConcurrentHashMap<>();

    private Long getTenantId() {
        Long tenantId = TenantContextHolder.getTenantId();
        return tenantId != null ? tenantId : 0L; // 0L as fallback for global/system
    }

    @Override
    public SpeedControlConfigRespVO get() {
        SpeedControlConfigDO config = speedControlConfigMapper.selectTheOne();
        SpeedControlConfigRespVO vo = new SpeedControlConfigRespVO();
        if (config == null) {
            return vo;
        }
        vo.setEnabled(config.getEnabled());
        vo.setRate(config.getRate());
        vo.setRangeEnabled(config.getRangeEnabled());
        vo.setMinMs(config.getMinMs());
        vo.setMaxMs(config.getMaxMs());
        vo.setRefFastMs(config.getRefFastMs());
        vo.setRefSlowMs(config.getRefSlowMs());
        vo.setMaxDelayMs(config.getMaxDelayMs());
        vo.setJitterPercent(config.getJitterPercent());
        vo.setExemptUserIds(new ArrayList<>(parseIds(config.getExemptUserIds())));
        vo.setExemptRoleIds(new ArrayList<>(parseIds(config.getExemptRoleIds())));
        return vo;
    }

    @Override
    public void update(SpeedControlConfigSaveReqVO reqVO) {
        SpeedControlConfigDO existing = speedControlConfigMapper.selectTheOne();
        SpeedControlConfigDO update = new SpeedControlConfigDO();
        if (existing != null) {
            update.setId(existing.getId());
        }
        update.setEnabled(Boolean.TRUE.equals(reqVO.getEnabled()));
        update.setRate(reqVO.getRate() == null ? new BigDecimal("100") : reqVO.getRate());
        update.setRangeEnabled(Boolean.TRUE.equals(reqVO.getRangeEnabled()));
        update.setMinMs(reqVO.getMinMs() == null ? 0L : reqVO.getMinMs());
        update.setMaxMs(reqVO.getMaxMs() == null ? 0L : reqVO.getMaxMs());
        update.setRefFastMs(reqVO.getRefFastMs() == null ? 100L : reqVO.getRefFastMs());
        update.setRefSlowMs(reqVO.getRefSlowMs() == null ? 10000L : reqVO.getRefSlowMs());
        update.setMaxDelayMs(reqVO.getMaxDelayMs() == null ? 600000L : reqVO.getMaxDelayMs());
        update.setJitterPercent(reqVO.getJitterPercent() == null ? BigDecimal.ZERO : reqVO.getJitterPercent());
        update.setExemptUserIds(joinIds(reqVO.getExemptUserIds()));
        update.setExemptRoleIds(joinIds(reqVO.getExemptRoleIds()));
        
        if (existing != null) {
            speedControlConfigMapper.updateById(update);
        } else {
            speedControlConfigMapper.insert(update);
        }
        tenantCache.remove(getTenantId()); // invalidate cache for this tenant
    }

    @Override
    public SpeedControlConfigDO getCachedConfig() {
        Long tenantId = getTenantId();
        long now = System.currentTimeMillis();
        CacheEntry entry = tenantCache.get(tenantId);
        if (entry != null && now - entry.cachedAt <= CACHE_TTL_MS) {
            return entry.config;
        }
        synchronized (tenantCache) {
            entry = tenantCache.get(tenantId);
            if (entry != null && System.currentTimeMillis() - entry.cachedAt <= CACHE_TTL_MS) {
                return entry.config;
            }
            entry = new CacheEntry();
            try {
                SpeedControlConfigDO config = speedControlConfigMapper.selectTheOne();
                entry.config = config;
                entry.exemptUserIds = config == null ? Collections.emptySet() : parseIds(config.getExemptUserIds());
                entry.exemptRoleIds = config == null ? Collections.emptySet() : parseIds(config.getExemptRoleIds());
            } catch (Exception e) {
                log.warn("[SpeedControl] 读取配置失败，本轮按不限速处理：{}", e.getMessage());
                entry.config = null;
            }
            entry.cachedAt = System.currentTimeMillis();
            tenantCache.put(tenantId, entry);
            return entry.config;
        }
    }

    @Override
    public boolean isExempt(Long userId) {
        if (userId == null) {
            return false;
        }
        getCachedConfig(); // Ensure cache is loaded
        CacheEntry entry = tenantCache.get(getTenantId());
        if (entry == null) {
            return false;
        }
        if (entry.exemptUserIds.contains(userId)) {
            return true;
        }
        if (entry.exemptRoleIds.isEmpty()) {
            return false;
        }
        try {
            Set<Long> roleIds = permissionService.getUserRoleIdListByUserIdFromCache(userId);
            if (roleIds == null || roleIds.isEmpty()) {
                return false;
            }
            for (Long roleId : roleIds) {
                if (entry.exemptRoleIds.contains(roleId)) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.warn("[SpeedControl] 解析用户角色失败 userId={}：{}", userId, e.getMessage());
        }
        return false;
    }

    private static Set<Long> parseIds(String raw) {
        if (StrUtil.isBlank(raw)) return Collections.emptySet();
        Set<Long> ids = new HashSet<>();
        for (String part : raw.split(",")) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) continue;
            try { ids.add(Long.parseLong(trimmed)); } catch (NumberFormatException ignored) {}
        }
        return ids;
    }

    private static String joinIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (Long id : ids) {
            if (id == null) continue;
            if (sb.length() > 0) sb.append(',');
            sb.append(id);
        }
        return sb.toString();
    }
}
