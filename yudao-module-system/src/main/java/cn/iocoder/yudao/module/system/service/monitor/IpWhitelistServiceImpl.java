package cn.iocoder.yudao.module.system.service.monitor;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.IpWhitelistDO;
import cn.iocoder.yudao.module.system.dal.mysql.monitor.IpWhitelistMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class IpWhitelistServiceImpl implements IpWhitelistService {

    private static final long CACHE_TTL_MS = 5 * 60 * 1000L;

    /** 内网放行开关的哨兵行标识（ip 唯一，不是真实 IP，不在页面展示） */
    public static final String INTERNAL_ALLOW_IP = "__INTERNAL_ALLOW__";
    private static final String SOURCE_SWITCH = "SWITCH";

    @Resource
    private IpWhitelistMapper ipWhitelistMapper;

    /** 精确 IP 白名单 */
    private volatile Set<String> exactCache = Collections.emptySet();
    /** 网段白名单，每项为 {网络地址, 掩码}（IPv4，long 表示） */
    private volatile List<long[]> cidrCache = Collections.emptyList();
    /** 是否放行内网 IP（由哨兵行 enabled 决定） */
    private volatile boolean internalAllowed = false;
    private volatile long cachedAt = 0L;

    @PostConstruct
    public void init() {
        try {
            refreshCache();
        } catch (Exception e) {
            log.warn("[IpWhitelist] 初始化缓存失败", e);
        }
    }

    @Override
    public boolean isWhitelisted(String ip) {
        if (StrUtil.isBlank(ip)) {
            return false;
        }
        if (System.currentTimeMillis() - cachedAt > CACHE_TTL_MS) {
            refreshCache();
        }
        String t = ip.trim();
        if (exactCache.contains(t)) {
            return true;
        }
        Long v = ipv4ToLong(t);
        if (v == null) {
            // 非 IPv4（如 IPv6），仅在放行内网时对回环/localhost 放行
            return internalAllowed
                    && cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.isInternalIp(t);
        }
        for (long[] c : cidrCache) {
            if ((v & c[1]) == c[0]) {
                return true;
            }
        }
        // 内网放行开关：开启时，私有/回环地址一律视为白名单
        return internalAllowed
                && cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.isInternalIp(t);
    }

    @Override
    public PageResult<IpWhitelistDO> getPage(PageParam pageParam) {
        return ipWhitelistMapper.selectPage(pageParam);
    }

    @Override
    public Long create(String ip, String remark) {
        IpWhitelistDO record = new IpWhitelistDO();
        record.setIp(StrUtil.trim(ip));
        record.setRemark(remark);
        record.setSource("MANUAL");
        record.setEnabled(1);
        record.setCreateTime(LocalDateTime.now());
        record.setDeleted(0);
        ipWhitelistMapper.insert(record);
        refreshCache();
        return record.getId();
    }

    @Override
    public int syncSourceIps(String source, List<String> ips) {
        if (StrUtil.isBlank(source) || ips == null || ips.isEmpty()) {
            return 0; // 列表为空直接跳过，避免把该来源整批误删
        }
        Set<String> want = new HashSet<>();
        for (String ip : ips) {
            String t = StrUtil.trim(ip);
            if (StrUtil.isNotBlank(t)) {
                want.add(t);
            }
        }
        List<IpWhitelistDO> existing = ipWhitelistMapper.selectListBySource(source);
        Set<String> have = new HashSet<>();
        List<Long> removeIds = new ArrayList<>();
        for (IpWhitelistDO d : existing) {
            String t = StrUtil.trim(d.getIp());
            if (want.contains(t)) {
                have.add(t);
            } else {
                removeIds.add(d.getId()); // 该来源里、最新列表已没有的 → 删
            }
        }
        if (!removeIds.isEmpty()) {
            ipWhitelistMapper.deleteByIds(removeIds);
        }
        for (String ip : want) {
            if (have.contains(ip)) {
                continue;
            }
            // ip 唯一：若已被其它来源（如 MANUAL）占用，则保留原样、不重复插入
            if (ipWhitelistMapper.selectOne(IpWhitelistDO::getIp, ip) != null) {
                continue;
            }
            IpWhitelistDO record = new IpWhitelistDO();
            record.setIp(ip);
            record.setRemark("微信官方回调IP getcallbackip");
            record.setSource(source);
            record.setEnabled(1);
            record.setCreateTime(LocalDateTime.now());
            record.setDeleted(0);
            ipWhitelistMapper.insert(record);
        }
        refreshCache();
        log.info("[IpWhitelist] 来源[{}]对账完成：目标{}个，删除{}个", source, want.size(), removeIds.size());
        return want.size();
    }

    @Override
    public void update(Long id, String ip, String remark, Integer enabled) {
        IpWhitelistDO record = new IpWhitelistDO();
        record.setId(id);
        record.setIp(StrUtil.trim(ip));
        record.setRemark(remark);
        record.setEnabled(enabled);
        record.setUpdateTime(LocalDateTime.now());
        ipWhitelistMapper.updateById(record);
        refreshCache();
    }

    @Override
    public void delete(Long id) {
        ipWhitelistMapper.deleteById(id);
        refreshCache();
    }

    @Override
    public boolean isInternalAllowed() {
        if (System.currentTimeMillis() - cachedAt > CACHE_TTL_MS) {
            refreshCache();
        }
        return internalAllowed;
    }

    @Override
    public void setInternalAllow(boolean enabled) {
        IpWhitelistDO row = ipWhitelistMapper.selectOne(IpWhitelistDO::getIp, INTERNAL_ALLOW_IP);
        if (row == null) {
            row = new IpWhitelistDO();
            row.setIp(INTERNAL_ALLOW_IP);
            row.setRemark("内网IP放行开关（系统）");
            row.setSource(SOURCE_SWITCH);
            row.setEnabled(enabled ? 1 : 0);
            row.setCreateTime(LocalDateTime.now());
            row.setDeleted(0);
            ipWhitelistMapper.insert(row);
        } else {
            row.setEnabled(enabled ? 1 : 0);
            row.setUpdateTime(LocalDateTime.now());
            ipWhitelistMapper.updateById(row);
        }
        refreshCache();
        log.warn("[IpWhitelist] 内网放行开关已{}", enabled ? "开启" : "关闭");
    }

    @Override
    public void refreshCache() {
        Set<String> exact = new HashSet<>();
        List<long[]> cidrs = new ArrayList<>();
        boolean internal = false;
        for (IpWhitelistDO d : ipWhitelistMapper.selectEnabledList()) {
            String s = StrUtil.trim(d.getIp());
            if (StrUtil.isBlank(s)) {
                continue;
            }
            if (INTERNAL_ALLOW_IP.equals(s)) {
                internal = true; // 哨兵行且已启用 → 放行内网
                continue;
            }
            if (s.contains("/")) {
                long[] cidr = parseCidr(s);
                if (cidr != null) {
                    cidrs.add(cidr);
                } else {
                    log.warn("[IpWhitelist] 非法网段，已忽略：{}", s);
                }
            } else {
                exact.add(s);
            }
        }
        exactCache = exact;
        cidrCache = cidrs;
        internalAllowed = internal;
        cachedAt = System.currentTimeMillis();
    }

    /** IPv4 转 long；非法返回 null */
    static Long ipv4ToLong(String ip) {
        if (ip == null) {
            return null;
        }
        String[] p = ip.split("\\.");
        if (p.length != 4) {
            return null;
        }
        long r = 0;
        for (String part : p) {
            try {
                int b = Integer.parseInt(part.trim());
                if (b < 0 || b > 255) {
                    return null;
                }
                r = (r << 8) | b;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return r & 0xFFFFFFFFL;
    }

    /** 解析 CIDR，返回 {网络地址, 掩码}；非法返回 null */
    static long[] parseCidr(String cidr) {
        int idx = cidr.indexOf('/');
        if (idx <= 0) {
            return null;
        }
        Long ipLong = ipv4ToLong(cidr.substring(0, idx));
        if (ipLong == null) {
            return null;
        }
        int prefix;
        try {
            prefix = Integer.parseInt(cidr.substring(idx + 1).trim());
        } catch (NumberFormatException e) {
            return null;
        }
        if (prefix < 0 || prefix > 32) {
            return null;
        }
        long mask = prefix == 0 ? 0L : ((0xFFFFFFFFL << (32 - prefix)) & 0xFFFFFFFFL);
        return new long[]{ipLong & mask, mask};
    }
}
