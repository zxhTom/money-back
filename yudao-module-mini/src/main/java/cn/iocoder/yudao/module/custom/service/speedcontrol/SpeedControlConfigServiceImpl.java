package cn.iocoder.yudao.module.custom.service.speedcontrol;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.custom.controller.admin.speedcontrol.vo.SpeedControlConfigRespVO;
import cn.iocoder.yudao.module.custom.controller.admin.speedcontrol.vo.SpeedControlConfigSaveReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.speedcontrol.SpeedControlConfigDO;
import cn.iocoder.yudao.module.custom.dal.mysql.speedcontrol.SpeedControlConfigMapper;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class SpeedControlConfigServiceImpl implements SpeedControlConfigService {

    private static final long CACHE_TTL_MS = 10_000L;

    @Resource
    private SpeedControlConfigMapper speedControlConfigMapper;
    @Resource
    private PermissionService permissionService;

    private volatile SpeedControlConfigDO cachedConfig;
    private volatile Set<Long> cachedExemptUserIds = Collections.emptySet();
    private volatile Set<Long> cachedExemptRoleIds = Collections.emptySet();
    private volatile long cachedAt;

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
        SpeedControlConfigDO update = new SpeedControlConfigDO();
        update.setId(1L);
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
        if (speedControlConfigMapper.selectById(1L) == null) {
            speedControlConfigMapper.insert(update);
        } else {
            speedControlConfigMapper.updateById(update);
        }
        cachedAt = 0L; // 立即失效，不用等 TTL
    }

    @Override
    public SpeedControlConfigDO getCachedConfig() {
        long now = System.currentTimeMillis();
        if (now - cachedAt <= CACHE_TTL_MS) {
            return cachedConfig;
        }
        synchronized (this) {
            if (System.currentTimeMillis() - cachedAt <= CACHE_TTL_MS) {
                return cachedConfig;
            }
            try {
                SpeedControlConfigDO config = speedControlConfigMapper.selectTheOne();
                cachedConfig = config;
                cachedExemptUserIds = config == null ? Collections.<Long>emptySet() : parseIds(config.getExemptUserIds());
                cachedExemptRoleIds = config == null ? Collections.<Long>emptySet() : parseIds(config.getExemptRoleIds());
            } catch (Exception e) {
                // 查库失败按"不限速"处理，绝不能因为限速功能自身故障把全站请求拖死
                log.warn("[SpeedControl] 读取配置失败，本轮按不限速处理：{}", e.getMessage());
                cachedConfig = null;
            }
            cachedAt = System.currentTimeMillis();
            return cachedConfig;
        }
    }

    @Override
    public boolean isExempt(Long userId) {
        if (userId == null) {
            return false; // 未登录请求同样受控（黑名单式）
        }
        getCachedConfig(); // 确保豁免名单快照是新的
        if (cachedExemptUserIds.contains(userId)) {
            return true;
        }
        if (cachedExemptRoleIds.isEmpty()) {
            return false;
        }
        try {
            Set<Long> roleIds = permissionService.getUserRoleIdListByUserIdFromCache(userId);
            if (roleIds == null || roleIds.isEmpty()) {
                return false;
            }
            for (Long roleId : roleIds) {
                if (cachedExemptRoleIds.contains(roleId)) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.warn("[SpeedControl] 解析用户角色失败 userId={}：{}", userId, e.getMessage());
        }
        return false;
    }

    private static Set<Long> parseIds(String raw) {
        if (StrUtil.isBlank(raw)) {
            return Collections.emptySet();
        }
        Set<Long> ids = new HashSet<>();
        for (String part : raw.split(",")) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            try {
                ids.add(Long.parseLong(trimmed));
            } catch (NumberFormatException ignored) {
                // 脏数据跳过，不影响其它 id
            }
        }
        return ids;
    }

    private static String joinIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Long id : ids) {
            if (id == null) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(id);
        }
        return sb.toString();
    }

}
