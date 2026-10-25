package cn.iocoder.yudao.module.system.service.monitor;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.infra.api.websocket.WebSocketSenderApi;
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
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
@Slf4j
public class GlobalDiagnoseAsyncTask {

    private static final String WS_PROGRESS = "diagnose-progress";
    private static final String WS_DONE     = "diagnose-done";
    private static final String WS_ERROR    = "diagnose-error";

    private static final String CACHE_KEY   = "ip_risk:%s";
    private static final String CACHE_SAFE  = "safe";
    private static final long   CACHE_TTL   = 3600;
    private static final int    MAX_UNCACHED = 30;

    private static final int FAIL_RATE_THRESHOLD      = 5;
    private static final int MULTI_IP_THRESHOLD       = 5;
    private static final int HIGH_FREQ_HOUR_THRESHOLD = 10;

    public static final String RELEASE_LOCK_LUA =
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
    public static final DefaultRedisScript<Long> RELEASE_LOCK_SCRIPT = new DefaultRedisScript<>(RELEASE_LOCK_LUA, Long.class);

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource private VoidmobIpCheckClient   ipCheckClient;
    @Resource private StringRedisTemplate    stringRedisTemplate;
    @Resource private SecurityAlertService   securityAlertService;
    @Resource private LoginLogMapper         loginLogMapper;
    @Resource private OperateLogMapper       operateLogMapper;
    @Resource private DataAccessLogMapper    dataAccessLogMapper;
    @Resource private WebSocketSenderApi     webSocketSenderApi;
    @Resource private GlobalDiagnoseAccessGuard diagnoseAccessGuard;

    public List<GlobalIpDiagnoseVO> scanAllIps(int days) {
        Long tenantId = cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder.getTenantId();
        return scanAllIps(tenantId, days);
    }

    public List<GlobalIpDiagnoseVO> scanAllIps(Long tenantId, int days) {
        if (tenantId == null) {
            throw cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0(403, "租户信息缺失");
        }
        return doScanCore(tenantId, days, null);
    }

    public List<UserBehaviorVO> analyzeUserBehavior() {
        Long tenantId = cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder.getTenantId();
        return analyzeUserBehavior(tenantId);
    }

    public List<UserBehaviorVO> analyzeUserBehavior(Long tenantId) {
        if (tenantId == null) {
            throw cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0(403, "租户信息缺失");
        }
        return doAnalyzeCore(tenantId, null);
    }

    @Async
    public void doScanAllIpsAsync(GlobalDiagnoseTaskContext context, String resultKey, String runningKey) {
        if (context == null || context.getTenantId() == null) {
            log.error("[GlobalDiagnose] IP扫描任务上下文或租户ID缺失，任务失败关闭");
            releaseLock(runningKey, context != null ? context.getLockToken() : null);
            return;
        }
        Long tenantId = context.getTenantId();
        Long userId = context.getUserId();
        int days = context.getDays() != null ? context.getDays() : 7;
        try {
            TenantUtils.execute(tenantId, () -> {
                try {
                    Map<String, Map<Long, IpUserInfoVO>> ipUserMap = buildIpUserMap(tenantId, days);
                    int total = (int) ipUserMap.keySet().stream().filter(ip -> !isPrivateIp(ip)).count();

                    List<GlobalIpDiagnoseVO> results = new ArrayList<>();
                    int[] processed = {0};
                    int[] uncachedCount = {0};

                    for (Map.Entry<String, Map<Long, IpUserInfoVO>> entry : ipUserMap.entrySet()) {
                        String ip = entry.getKey();
                        List<IpUserInfoVO> users = new ArrayList<>(entry.getValue().values());

                        GlobalIpDiagnoseVO vo = buildBaseVo(ip, users);

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
                        } else if (uncachedCount[0] < MAX_UNCACHED) {
                            callApiAndFill(vo, ip);
                            uncachedCount[0]++;
                        } else {
                            vo.setError("本次扫描已达外部接口上限，待下次扫描补充");
                        }

                        results.add(vo);
                        processed[0]++;

                        sendProgress(context, processed[0], total, "正在检查 " + ip);
                    }

                    results.sort((a, b) -> Boolean.compare(b.isRisky(), a.isRisky()));

                    GlobalDiagnoseCacheEnvelope envelope = GlobalDiagnoseCacheEnvelope.builder()
                            .tenantId(tenantId)
                            .userId(userId)
                            .taskType("scan-ips")
                            .days(days)
                            .createTime(System.currentTimeMillis())
                            .dataJson(JSON.toJSONString(results))
                            .build();

                    stringRedisTemplate.opsForValue().set(resultKey, JSON.toJSONString(envelope), 1800, TimeUnit.SECONDS);
                    sendDone(context, results);
                    log.info("[GlobalDiagnose] IP扫描完成，tenantId={} userId={} 共{}个IP", tenantId, userId, results.size());
                } catch (Exception e) {
                    log.error("[GlobalDiagnose] IP扫描执行异常", e);
                    sendError(context, "IP扫描处理异常");
                }
            });
        } catch (Exception e) {
            log.error("[GlobalDiagnose] IP扫描调度异常", e);
            sendError(context, "IP扫描任务调度异常");
        } finally {
            releaseLock(runningKey, context.getLockToken());
        }
    }

    @Async
    public void doAnalyzeBehaviorAsync(GlobalDiagnoseTaskContext context, String resultKey, String runningKey) {
        if (context == null || context.getTenantId() == null) {
            log.error("[GlobalDiagnose] 用户行为分析任务上下文或租户ID缺失，任务失败关闭");
            releaseLock(runningKey, context != null ? context.getLockToken() : null);
            return;
        }
        Long tenantId = context.getTenantId();
        Long userId = context.getUserId();
        try {
            TenantUtils.execute(tenantId, () -> {
                try {
                    sendProgress(context, 0, 1, "正在加载用户行为数据...");
                    List<UserBehaviorVO> result = doAnalyzeCore(tenantId, null);

                    GlobalDiagnoseCacheEnvelope envelope = GlobalDiagnoseCacheEnvelope.builder()
                            .tenantId(tenantId)
                            .userId(userId)
                            .taskType("user-behavior")
                            .createTime(System.currentTimeMillis())
                            .dataJson(JSON.toJSONString(result))
                            .build();

                    stringRedisTemplate.opsForValue().set(resultKey, JSON.toJSONString(envelope), 1800, TimeUnit.SECONDS);
                    sendDone(context, result);
                    log.info("[GlobalDiagnose] 用户行为分析完成，tenantId={} userId={} 共{}名用户", tenantId, userId, result.size());
                } catch (Exception e) {
                    log.error("[GlobalDiagnose] 用户行为分析执行异常", e);
                    sendError(context, "用户行为分析处理异常");
                }
            });
        } catch (Exception e) {
            log.error("[GlobalDiagnose] 用户行为分析调度异常", e);
            sendError(context, "用户行为分析任务调度异常");
        } finally {
            releaseLock(runningKey, context.getLockToken());
        }
    }

    private void releaseLock(String runningKey, String lockToken) {
        if (runningKey == null || lockToken == null) {
            return;
        }
        try {
            stringRedisTemplate.execute(RELEASE_LOCK_SCRIPT, Collections.singletonList(runningKey), lockToken);
        } catch (Exception e) {
            log.warn("[GlobalDiagnose] 释放锁异常 runningKey={}", runningKey, e);
        }
    }

    private List<GlobalIpDiagnoseVO> doScanCore(Long tenantId, int days, Consumer<Map<String, Object>> progressReporter) {
        Map<String, Map<Long, IpUserInfoVO>> ipUserMap = buildIpUserMap(tenantId, days);
        int total = ipUserMap.size();
        int[] processed = {0};
        int uncachedCount = 0;
        List<GlobalIpDiagnoseVO> results = new ArrayList<>();

        for (Map.Entry<String, Map<Long, IpUserInfoVO>> entry : ipUserMap.entrySet()) {
            String ip = entry.getKey();
            List<IpUserInfoVO> users = new ArrayList<>(entry.getValue().values());

            GlobalIpDiagnoseVO vo = buildBaseVo(ip, users);

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
            processed[0]++;

            if (progressReporter != null) {
                Map<String, Object> progress = new HashMap<>();
                progress.put("processed", processed[0]);
                progress.put("total", total);
                progress.put("currentIp", ip);
                progressReporter.accept(progress);
            }
        }

        results.sort((a, b) -> Boolean.compare(b.isRisky(), a.isRisky()));
        return results;
    }

    private List<UserBehaviorVO> doAnalyzeCore(Long tenantId, Consumer<int[]> progressReporter) {
        List<LoginUserStatsDO>    stats     = loginLogMapper.selectDiagnoseUserLoginStats(tenantId);
        List<Map<String, Object>> hourStats = loginLogMapper.selectDiagnoseUserLoginCountLastHour(tenantId);

        Map<Long, Integer> hourMap = new HashMap<>();
        for (Map<String, Object> row : hourStats) {
            Long uid = toLong(row.get("userId"));
            if (uid != null) hourMap.put(uid, toInt(row.get("loginCountLastHour")));
        }

        List<UserBehaviorVO> results = new ArrayList<>();
        int total = stats.size();
        int i = 0;

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
            vo.setLastLoginIp(loginLogMapper.selectDiagnoseLastLoginIp(tenantId, userId));
            vo.setActivityLevel(calcActivityLevel(lastLoginStr));

            List<String> abnormalIps = findAbnormalIps(tenantId, userId);
            vo.setAbnormalIps(abnormalIps);

            List<String> reasons = buildSuspiciousReasons(vo);
            vo.setSuspiciousReasons(reasons);
            vo.setSuspicious(!reasons.isEmpty());

            results.add(vo);
            i++;

            if (progressReporter != null) {
                progressReporter.accept(new int[]{i, total});
            }
        }

        results.sort((a, b) -> {
            if (a.isSuspicious() != b.isSuspicious()) return Boolean.compare(b.isSuspicious(), a.isSuspicious());
            return Integer.compare(b.getLoginCount(), a.getLoginCount());
        });
        return results;
    }

    private void sendProgress(GlobalDiagnoseTaskContext context, int current, int total, String message) {
        TenantUtils.execute(context.getTenantId(), () -> {
            if (!diagnoseAccessGuard.canPushResult(context.getTenantId(), context.getUserId())) {
                log.info("[GlobalDiagnose] 进度推送前鉴权未通过 tenantId={} userId={}",
                        context.getTenantId(), context.getUserId());
                return;
            }
            Map<String, Object> msg = new LinkedHashMap<>();
            msg.put("taskType", context.getTaskType());
            msg.put("current", current);
            msg.put("total", total);
            msg.put("message", message);
            webSocketSenderApi.sendObject(UserTypeEnum.ADMIN.getValue(), context.getUserId(), WS_PROGRESS, msg);
        });
    }

    private void sendDone(GlobalDiagnoseTaskContext context, Object data) {
        TenantUtils.execute(context.getTenantId(), () -> {
            if (!diagnoseAccessGuard.canPushResult(context.getTenantId(), context.getUserId())) {
                log.info("[GlobalDiagnose] 完成推送前鉴权未通过 tenantId={} userId={}",
                        context.getTenantId(), context.getUserId());
                return;
            }
            Map<String, Object> msg = new LinkedHashMap<>();
            msg.put("taskType", context.getTaskType());
            msg.put("data", data);
            webSocketSenderApi.sendObject(UserTypeEnum.ADMIN.getValue(), context.getUserId(), WS_DONE, msg);
        });
    }

    private void sendError(GlobalDiagnoseTaskContext context, String error) {
        TenantUtils.execute(context.getTenantId(), () -> {
            if (!diagnoseAccessGuard.canPushResult(context.getTenantId(), context.getUserId())) {
                log.info("[GlobalDiagnose] 错误推送前鉴权未通过 tenantId={} userId={}",
                        context.getTenantId(), context.getUserId());
                return;
            }
            Map<String, Object> msg = new LinkedHashMap<>();
            msg.put("taskType", context.getTaskType());
            msg.put("error", error != null ? error : "任务执行失败");
            webSocketSenderApi.sendObject(UserTypeEnum.ADMIN.getValue(), context.getUserId(), WS_ERROR, msg);
        });
    }

    private Map<String, Map<Long, IpUserInfoVO>> buildIpUserMap(Long tenantId, int days) {
        Map<String, Map<Long, IpUserInfoVO>> ipUserMap = new LinkedHashMap<>();
        mergeRefs(loginLogMapper.selectDiagnoseIpUserRefs(tenantId, days),      "登录日志",     ipUserMap);
        mergeRefs(operateLogMapper.selectDiagnoseIpUserRefs(tenantId, days),    "操作日志",     ipUserMap);
        mergeRefs(dataAccessLogMapper.selectDiagnoseIpUserRefs(tenantId, days), "数据访问日志",  ipUserMap);
        return ipUserMap;
    }

    private GlobalIpDiagnoseVO buildBaseVo(String ip, List<IpUserInfoVO> users) {
        GlobalIpDiagnoseVO vo = new GlobalIpDiagnoseVO();
        vo.setIp(ip);
        vo.setUsers(users);
        vo.setFirstSeen(users.stream().map(IpUserInfoVO::getLastSeen).filter(Objects::nonNull).min(Comparator.naturalOrder()).orElse(null));
        vo.setLastSeen(users.stream().map(IpUserInfoVO::getLastSeen).filter(Objects::nonNull).max(Comparator.naturalOrder()).orElse(null));
        return vo;
    }

    private void mergeRefs(List<IpUserRefDO> rows, String source,
                           Map<String, Map<Long, IpUserInfoVO>> ipUserMap) {
        if (rows == null) return;
        for (IpUserRefDO row : rows) {
            String ip = row.getIp();
            if (ip == null || ip.trim().isEmpty()) continue;
            Long userId = row.getUserId();
            if (userId == null) continue;
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
        List<String> risks = JSON.parseArray(cached, String.class);
        if (risks != null) { vo.setRisky(!risks.isEmpty()); vo.setRisks(risks); }
    }

    private List<String> findAbnormalIps(Long tenantId, Long userId) {
        List<String> allIps = new ArrayList<>();
        allIps.addAll(loginLogMapper.selectDiagnoseDistinctIpsByUserId(tenantId, userId));
        allIps.addAll(dataAccessLogMapper.selectDiagnoseDistinctIpsByUserId(tenantId, userId));

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
            LocalDateTime last = LocalDateTime.parse(lastLoginStr.replace(".0", ""), FMT);
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

    private Long toLong(Object v) { if (v == null) return null; try { return Long.parseLong(v.toString()); } catch (Exception e) { return null; } }
    private int  toInt(Object v)  { if (v == null) return 0; try { return Integer.parseInt(v.toString()); } catch (Exception e) { return 0; } }
}
