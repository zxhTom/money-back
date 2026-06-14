package cn.iocoder.yudao.module.system.service.monitor;

import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.GlobalIpDiagnoseVO;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.IpUserInfoVO;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.UserBehaviorVO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.IpUserRefDO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.LoginUserStatsDO;
import cn.iocoder.yudao.module.system.dal.mysql.logger.LoginLogMapper;
import cn.iocoder.yudao.module.system.dal.mysql.logger.OperateLogMapper;
import cn.iocoder.yudao.module.system.dal.mysql.monitor.DataAccessLogMapper;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GlobalDiagnoseServiceImpl implements GlobalDiagnoseService {

    private static final String CACHE_KEY   = "ip_risk:%s";
    private static final String CACHE_SAFE  = "safe";
    private static final long   CACHE_TTL   = 3600;
    /** 单次扫描最多调用外部接口的未缓存 IP 数，避免耗尽 API 额度 */
    private static final int    MAX_UNCACHED = 30;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 用户行为异常阈值
    private static final int FAIL_RATE_THRESHOLD      = 5;   // 30天失败次数 > 5
    private static final int MULTI_IP_THRESHOLD       = 5;   // 30天不同 IP > 5
    private static final int HIGH_FREQ_HOUR_THRESHOLD = 10;  // 1小时登录 > 10

    @Resource private VoidmobIpCheckClient   ipCheckClient;
    @Resource private StringRedisTemplate    stringRedisTemplate;
    @Resource private SecurityAlertService   securityAlertService;
    @Resource private LoginLogMapper        loginLogMapper;
    @Resource private OperateLogMapper      operateLogMapper;
    @Resource private DataAccessLogMapper   dataAccessLogMapper;

    // ─── IP 扫描 ──────────────────────────────────────────────

    @Override
    public List<GlobalIpDiagnoseVO> scanAllIps(int days) {
        // 1. 从三个日志来源汇聚: ip → { userId → IpUserInfoVO }
        Map<String, Map<Long, IpUserInfoVO>> ipUserMap = new LinkedHashMap<>();

        mergeRefs(loginLogMapper.selectIpUserRefs(days),      "登录日志",     ipUserMap);
        mergeRefs(operateLogMapper.selectIpUserRefs(days),    "操作日志",     ipUserMap);
        mergeRefs(dataAccessLogMapper.selectIpUserRefs(days), "数据访问日志",  ipUserMap);

        // 2. 逐 IP 构建 VO，已缓存直接读缓存，未缓存逐一调接口（限量）
        List<GlobalIpDiagnoseVO> results = new ArrayList<>();
        int uncachedCount = 0;

        for (Map.Entry<String, Map<Long, IpUserInfoVO>> entry : ipUserMap.entrySet()) {
            String ip = entry.getKey();
            List<IpUserInfoVO> users = new ArrayList<>(entry.getValue().values());

            GlobalIpDiagnoseVO vo = new GlobalIpDiagnoseVO();
            vo.setIp(ip);
            vo.setUsers(users);
            // firstSeen/lastSeen 取所有用户的最早/最晚
            vo.setFirstSeen(users.stream().map(IpUserInfoVO::getLastSeen).filter(Objects::nonNull).min(Comparator.naturalOrder()).orElse(null));
            vo.setLastSeen(users.stream().map(IpUserInfoVO::getLastSeen).filter(Objects::nonNull).max(Comparator.naturalOrder()).orElse(null));

            if (isPrivateIp(ip)) {
                vo.setRisky(false);
                vo.setRisks(Collections.emptyList());
                vo.setType("private");
                vo.setCountry("内网");
                results.add(vo);
                continue;
            }

            String cacheKey = String.format(CACHE_KEY, ip);
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);

            if (cached != null) {
                vo.setCached(true);
                applyCache(vo, cached);
            } else if (uncachedCount < MAX_UNCACHED) {
                callApiAndFill(vo, ip);
                uncachedCount++;
            } else {
                vo.setError("本次扫描已达外部接口上限，待下次扫描补充");
            }

            results.add(vo);
        }

        // 风险 IP 排前面
        results.sort((a, b) -> Boolean.compare(b.isRisky(), a.isRisky()));
        return results;
    }

    // ─── 用户行为分析 ─────────────────────────────────────────

    @Override
    public List<UserBehaviorVO> analyzeUserBehavior() {
        List<LoginUserStatsDO>    stats     = loginLogMapper.selectUserLoginStats();
        List<Map<String, Object>> hourStats = loginLogMapper.selectUserLoginCountLastHour();

        Map<Long, Integer> hourMap = new HashMap<>();
        for (Map<String, Object> row : hourStats) {
            Long uid = toLong(row.get("userId"));
            if (uid != null) hourMap.put(uid, toInt(row.get("loginCountLastHour")));
        }

        List<UserBehaviorVO> results = new ArrayList<>();
        for (LoginUserStatsDO stat : stats) {
            Long userId = stat.getUserId();
            if (userId == null) continue;

            UserBehaviorVO vo = new UserBehaviorVO();
            vo.setUserId(userId);
            vo.setUsername(stat.getUsername());
            vo.setLoginCount(stat.getLoginCount() != null ? stat.getLoginCount() : 0);
            vo.setFailCount(stat.getFailCount() != null ? stat.getFailCount() : 0);
            vo.setDistinctIpCount(stat.getDistinctIpCount() != null ? stat.getDistinctIpCount() : 0);
            vo.setLoginCountLastHour(hourMap.getOrDefault(userId, 0));

            String lastLoginStr = stat.getLastLoginTime() != null ? stat.getLastLoginTime().format(FMT) : null;
            vo.setLastLoginTime(lastLoginStr);
            vo.setLastLoginIp(loginLogMapper.selectLastLoginIp(userId));
            vo.setActivityLevel(calcActivityLevel(lastLoginStr));

            List<String> abnormalIps = findAbnormalIps(userId);
            vo.setAbnormalIps(abnormalIps);

            List<String> reasons = buildSuspiciousReasons(vo);
            vo.setSuspiciousReasons(reasons);
            vo.setSuspicious(!reasons.isEmpty());

            results.add(vo);
        }

        results.sort((a, b) -> {
            if (a.isSuspicious() != b.isSuspicious()) return Boolean.compare(b.isSuspicious(), a.isSuspicious());
            return Integer.compare(b.getLoginCount(), a.getLoginCount());
        });
        return results;
    }

    // ─── 内部工具 ─────────────────────────────────────────────

    private void mergeRefs(List<IpUserRefDO> rows, String source,
                           Map<String, Map<Long, IpUserInfoVO>> ipUserMap) {
        if (rows == null) return;
        for (IpUserRefDO row : rows) {
            String ip     = row.getIp();
            Long   userId = row.getUserId();
            if (ip == null || userId == null) continue;

            String lastSeen = row.getLastSeen();
            String username = row.getUsername();

            Map<Long, IpUserInfoVO> userMap = ipUserMap.computeIfAbsent(ip, k -> new LinkedHashMap<>());
            IpUserInfoVO info = userMap.computeIfAbsent(userId, k -> {
                IpUserInfoVO v = new IpUserInfoVO();
                v.setUserId(userId);
                v.setUsername(username != null ? username : String.valueOf(userId));
                v.setSources(new ArrayList<>());
                return v;
            });
            if (!info.getSources().contains(source)) info.getSources().add(source);
            if (username != null && info.getUsername().equals(String.valueOf(userId))) info.setUsername(username);
            if (lastSeen != null && (info.getLastSeen() == null || lastSeen.compareTo(info.getLastSeen()) > 0)) {
                info.setLastSeen(lastSeen);
            }
        }
    }

    private void callApiAndFill(GlobalIpDiagnoseVO vo, String ip) {
        JSONObject resp = ipCheckClient.check(ip);
        if (resp == null) {
            vo.setError("检测失败");
            return;
        }

        List<String> risks = extractRisks(resp);
        vo.setRisky(!risks.isEmpty());
        vo.setRisks(risks);
        vo.setType(resp.getString("type"));
        vo.setCarrier(resp.getString("carrier"));
        JSONObject loc = resp.getJSONObject("location");
        if (loc != null) { vo.setCountry(loc.getString("country")); vo.setCity(loc.getString("city")); }

        JSONObject cacheObj = new JSONObject();
        cacheObj.put("risks", risks);
        cacheObj.put("type", vo.getType());
        cacheObj.put("carrier", vo.getCarrier());
        cacheObj.put("country", vo.getCountry());
        cacheObj.put("city", vo.getCity());
        stringRedisTemplate.opsForValue().set(String.format(CACHE_KEY, ip), cacheObj.toJSONString(), CACHE_TTL, TimeUnit.SECONDS);

        if (!risks.isEmpty()) {
            int severity = risks.contains("TOR") || risks.contains("ABUSER") ? 3 : 2;
            String msg = String.format("全局扫描发现风险IP [%s]：%s (%s %s)",
                    ip, String.join("/", risks), vo.getCountry(), vo.getCity());
            securityAlertService.saveAsync("IP_RISK", severity, ip, null, "/global-diagnose", "SCAN", null, msg);
        }
    }

    private void applyCache(GlobalIpDiagnoseVO vo, String cached) {
        if (CACHE_SAFE.equals(cached)) {
            vo.setRisky(false);
            vo.setRisks(Collections.emptyList());
            return;
        }
        try {
            JSONObject obj = JSON.parseObject(cached);
            if (obj != null && obj.containsKey("risks")) {
                List<String> risks = obj.getJSONArray("risks").toJavaList(String.class);
                vo.setRisky(!risks.isEmpty());
                vo.setRisks(risks);
                vo.setType(obj.getString("type"));
                vo.setCarrier(obj.getString("carrier"));
                vo.setCountry(obj.getString("country"));
                vo.setCity(obj.getString("city"));
                return;
            }
        } catch (Exception ignored) {}
        // 兼容旧格式（纯 risks 数组）
        List<String> risks = JSON.parseArray(cached, String.class);
        if (risks != null) { vo.setRisky(!risks.isEmpty()); vo.setRisks(risks); }
    }

    private List<String> findAbnormalIps(Long userId) {
        List<String> allIps = new ArrayList<>();
        allIps.addAll(loginLogMapper.selectDistinctIpsByUserId(userId));
        allIps.addAll(dataAccessLogMapper.selectDistinctIpsByUserId(userId));

        return allIps.stream().distinct()
                .filter(ip -> {
                    String cached = stringRedisTemplate.opsForValue().get(String.format(CACHE_KEY, ip));
                    if (cached == null || CACHE_SAFE.equals(cached)) return false;
                    try {
                        JSONObject obj = JSON.parseObject(cached);
                        if (obj != null && obj.containsKey("risks")) {
                            return !obj.getJSONArray("risks").isEmpty();
                        }
                    } catch (Exception ignored) {}
                    // 旧格式
                    List<String> risks = JSON.parseArray(cached, String.class);
                    return risks != null && !risks.isEmpty();
                })
                .collect(Collectors.toList());
    }

    private List<String> buildSuspiciousReasons(UserBehaviorVO vo) {
        List<String> reasons = new ArrayList<>();
        if (vo.getFailCount() > FAIL_RATE_THRESHOLD)
            reasons.add(String.format("近30天登录失败 %d 次", vo.getFailCount()));
        if (vo.getDistinctIpCount() > MULTI_IP_THRESHOLD)
            reasons.add(String.format("近30天使用 %d 个不同 IP", vo.getDistinctIpCount()));
        if (vo.getLoginCountLastHour() > HIGH_FREQ_HOUR_THRESHOLD)
            reasons.add(String.format("近1小时登录 %d 次（高频）", vo.getLoginCountLastHour()));
        if (!vo.getAbnormalIps().isEmpty())
            reasons.add("使用了风险 IP：" + String.join(", ", vo.getAbnormalIps()));
        return reasons;
    }

    private String calcActivityLevel(String lastLoginStr) {
        if (lastLoginStr == null) return "INACTIVE";
        try {
            LocalDateTime last = LocalDateTime.parse(lastLoginStr.replace(".0", ""),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            long days = java.time.Duration.between(last, LocalDateTime.now()).toDays();
            if (days <= 7)  return "ACTIVE";
            if (days <= 30) return "NORMAL";
            return "INACTIVE";
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    private List<String> extractRisks(JSONObject resp) {
        List<String> risks = new ArrayList<>();
        if (Boolean.TRUE.equals(resp.getBoolean("isProxy")))   risks.add("PROXY");
        if (Boolean.TRUE.equals(resp.getBoolean("isVpn")))     risks.add("VPN");
        if (Boolean.TRUE.equals(resp.getBoolean("isTor")))     risks.add("TOR");
        if (Boolean.TRUE.equals(resp.getBoolean("isAbuser")))  risks.add("ABUSER");
        if (Boolean.TRUE.equals(resp.getBoolean("isCrawler"))) risks.add("CRAWLER");
        return risks;
    }

    private boolean isPrivateIp(String ip) {
        return ip.startsWith("127.") || ip.startsWith("10.") || ip.startsWith("192.168.")
                || ip.startsWith("172.16.") || ip.startsWith("172.17.") || ip.startsWith("172.18.")
                || ip.startsWith("172.19.") || ip.startsWith("172.2")   || ip.startsWith("172.30.")
                || ip.startsWith("172.31.") || ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1");
    }

    private String str(Object v)  { return v != null ? v.toString() : null; }
    private Long   toLong(Object v) { if (v == null) return null; try { return Long.parseLong(v.toString()); } catch (Exception e) { return null; } }
    private int    toInt(Object v)  { if (v == null) return 0; try { return Integer.parseInt(v.toString()); } catch (Exception e) { return 0; } }
}
