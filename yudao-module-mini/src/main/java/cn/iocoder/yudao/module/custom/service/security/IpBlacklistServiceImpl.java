package cn.iocoder.yudao.module.custom.service.security;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.custom.controller.admin.security.vo.IpBlacklistAddReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.security.IpBlacklistDO;
import cn.iocoder.yudao.module.custom.dal.mysql.security.IpBlacklistMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class IpBlacklistServiceImpl implements IpBlacklistService {

    private static final String BLACKLIST_CACHE_KEY = "security:ip:blacklist";
    // 缓存有效期 5 分钟，到期后下次查询时重建
    private static final long CACHE_TTL_SECONDS = 300;

    @Resource
    private IpBlacklistMapper ipBlacklistMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @PostConstruct
    public void init() {
        try {
            refreshCache();
        } catch (Exception e) {
            log.warn("[IpBlacklist] 初始化缓存失败，将在首次查询时重建", e);
        }
    }

    @Override
    public boolean isBlacklisted(String ip) {
        if (ip == null) return false;
        // 先查 Redis 缓存
        Boolean member = stringRedisTemplate.opsForSet().isMember(BLACKLIST_CACHE_KEY, ip);
        if (Boolean.TRUE.equals(member)) return true;
        // 缓存可能已失效，走 DB 确认
        return ipBlacklistMapper.countActiveByIp(ip) > 0;
    }

    @Override
    public void addToBlacklist(String ip, String reason, boolean autoAdded, LocalDateTime expireTime) {
        // 已存在则更新，否则插入
        IpBlacklistDO existing = ipBlacklistMapper.selectOne(
                new LambdaQueryWrapper<IpBlacklistDO>().eq(IpBlacklistDO::getIp, ip));
        if (existing != null) {
            existing.setReason(reason);
            existing.setExpireTime(expireTime);
            ipBlacklistMapper.updateById(existing);
        } else {
            IpBlacklistDO blacklist = new IpBlacklistDO();
            blacklist.setIp(ip);
            blacklist.setReason(reason);
            blacklist.setAutoAdded(autoAdded);
            blacklist.setExpireTime(expireTime);
            blacklist.setCreateTime(LocalDateTime.now());
            ipBlacklistMapper.insert(blacklist);
        }
        // 同步到 Redis 缓存
        stringRedisTemplate.opsForSet().add(BLACKLIST_CACHE_KEY, ip);
        log.warn("[IpBlacklist] IP 已加入黑名单: {} 原因: {}", ip, reason);
    }

    @Override
    public void addToBlacklist(IpBlacklistAddReqVO reqVO) {
        addToBlacklist(reqVO.getIp(), reqVO.getReason(), false, reqVO.getExpireTime());
    }

    @Override
    public void removeFromBlacklist(Long id) {
        IpBlacklistDO blacklist = ipBlacklistMapper.selectById(id);
        if (blacklist != null) {
            ipBlacklistMapper.deleteById(id);
            stringRedisTemplate.opsForSet().remove(BLACKLIST_CACHE_KEY, blacklist.getIp());
        }
    }

    @Override
    public PageResult<IpBlacklistDO> getPage(PageParam pageParam) {
        return ipBlacklistMapper.selectPage(pageParam, new LambdaQueryWrapperX<IpBlacklistDO>()
                .orderByDesc(IpBlacklistDO::getId));
    }

    @Override
    public void refreshCache() {
        List<String> activeIps = ipBlacklistMapper.selectAllActiveIps();
        if (!activeIps.isEmpty()) {
            stringRedisTemplate.delete(BLACKLIST_CACHE_KEY);
            Set<String> ipSet = activeIps.stream().collect(Collectors.toSet());
            stringRedisTemplate.opsForSet().add(BLACKLIST_CACHE_KEY, ipSet.toArray(new String[0]));
            stringRedisTemplate.expire(BLACKLIST_CACHE_KEY, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        }
        log.info("[IpBlacklist] 黑名单缓存已刷新，共 {} 个IP", activeIps.size());
    }
}
