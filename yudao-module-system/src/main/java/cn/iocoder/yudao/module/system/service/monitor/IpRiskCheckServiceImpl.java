package cn.iocoder.yudao.module.system.service.monitor;

import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.IpDiagnoseResultVO;
import cn.iocoder.yudao.module.system.dal.mysql.logger.LoginLogMapper;
import cn.iocoder.yudao.module.system.dal.mysql.logger.OperateLogMapper;
import cn.iocoder.yudao.module.system.dal.mysql.monitor.DataAccessLogMapper;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class IpRiskCheckServiceImpl implements IpRiskCheckService {

    private static final String CHECK_URL = "https://voidmob.com/api/tools/ip-check?ip=%s";
    private static final String CACHE_KEY = "ip_risk:%s";
    private static final long CACHE_TTL_SECONDS = 3600;
    private static final String CACHE_SAFE = "safe";

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private SecurityAlertService securityAlertService;
    @Resource
    private LoginLogMapper loginLogMapper;
    @Resource
    private OperateLogMapper operateLogMapper;
    @Resource
    private DataAccessLogMapper dataAccessLogMapper;

    // ─── 异步过滤器调用 ───────────────────────────────────────

    @Override
    @Async
    public void checkAsync(String ip, Long userId, String requestUrl) {
        if (ip == null || ip.trim().isEmpty() || isPrivateIp(ip)) return;

        String cacheKey = String.format(CACHE_KEY, ip);
        if (stringRedisTemplate.opsForValue().get(cacheKey) != null) return; // 已检测过

        try {
            String body = HttpUtil.get(String.format(CHECK_URL, ip), 5000);
            JSONObject resp = JSON.parseObject(body);
            if (resp == null) return;

            List<String> risks = extractRisks(resp);
            writeCache(ip, resp, risks);
            if (risks.isEmpty()) return;

            int severity = risks.contains("TOR") || risks.contains("ABUSER") ? 3 : 2;
            String msg = String.format("IP [%s] 检测到风险类型：%s %s", ip, String.join("/", risks), buildLocation(resp));
            log.warn("[IpRiskCheck] {}", msg);
            securityAlertService.saveAsync("IP_RISK", severity, ip, userId, requestUrl, "REQUEST",
                    body.length() > 500 ? body.substring(0, 500) : body, msg);
        } catch (Exception e) {
            log.warn("[IpRiskCheck] 检测失败 ip={} err={}", ip, e.getMessage());
        }
    }

    // ─── 同步用户诊断 ─────────────────────────────────────────

    @Override
    public List<IpDiagnoseResultVO> diagnoseUser(Long userId) {
        // 1. 收集三个来源的 IP，记录每个 IP 从哪里来
        Map<String, List<String>> ipSources = new LinkedHashMap<>();
        collectIps(loginLogMapper.selectDistinctIpsByUserId(userId),   "登录日志",   ipSources);
        collectIps(operateLogMapper.selectDistinctIpsByUserId(userId),  "操作日志",   ipSources);
        collectIps(dataAccessLogMapper.selectDistinctIpsByUserId(userId), "数据访问日志", ipSources);

        // 2. 逐个检测
        List<IpDiagnoseResultVO> results = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : ipSources.entrySet()) {
            String ip = entry.getKey();
            IpDiagnoseResultVO vo = buildDiagnoseResult(ip, userId);
            vo.setSources(entry.getValue());
            results.add(vo);
        }

        // 3. 风险 IP 排在前面
        results.sort((a, b) -> Boolean.compare(b.isRisky(), a.isRisky()));
        return results;
    }

    private void collectIps(List<String> ips, String source, Map<String, List<String>> ipSources) {
        if (ips == null) return;
        for (String ip : ips) {
            if (ip == null || ip.trim().isEmpty()) continue;
            ipSources.computeIfAbsent(ip, k -> new ArrayList<>()).add(source);
        }
    }

    private IpDiagnoseResultVO buildDiagnoseResult(String ip, Long userId) {
        IpDiagnoseResultVO vo = new IpDiagnoseResultVO();
        vo.setIp(ip);

        if (isPrivateIp(ip)) {
            vo.setRisky(false);
            vo.setRisks(Collections.emptyList());
            vo.setType("private");
            vo.setCountry("内网");
            vo.setCity("-");
            vo.setCarrier("-");
            return vo;
        }

        String cacheKey = String.format(CACHE_KEY, ip);
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            vo.setCached(true);
            applyToVo(vo, cached);
            return vo;
        }

        try {
            String body = HttpUtil.get(String.format(CHECK_URL, ip), 8000);
            JSONObject resp = JSON.parseObject(body);
            if (resp == null) {
                vo.setError("接口返回为空");
                return vo;
            }

            List<String> risks = extractRisks(resp);
            vo.setRisky(!risks.isEmpty());
            vo.setRisks(risks);
            vo.setType(resp.getString("type"));
            vo.setCarrier(resp.getString("carrier"));

            JSONObject loc = resp.getJSONObject("location");
            if (loc != null) {
                vo.setCountry(loc.getString("country"));
                vo.setCity(loc.getString("city"));
            }

            writeCache(ip, resp, risks);

            // 有风险则写告警
            if (!risks.isEmpty()) {
                int severity = risks.contains("TOR") || risks.contains("ABUSER") ? 3 : 2;
                String msg = String.format("IP [%s] 经用户诊断发现风险：%s (%s %s)",
                        ip, String.join("/", risks), vo.getCountry(), vo.getCity());
                log.warn("[IpRiskCheck] {}", msg);
                securityAlertService.saveAsync("IP_RISK", severity, ip, userId, "/user-diagnose", "DIAGNOSE",
                        body.length() > 500 ? body.substring(0, 500) : body, msg);
            }
        } catch (Exception e) {
            log.warn("[IpRiskCheck] 诊断失败 ip={} err={}", ip, e.getMessage());
            vo.setError("检测失败：" + e.getMessage());
        }

        return vo;
    }

    // ─── 缓存读写（存完整信息） ────────────────────────────────

    /** 将 API 响应中的关键字段写入缓存（JSON 对象格式） */
    private void writeCache(String ip, JSONObject resp, List<String> risks) {
        JSONObject cacheObj = new JSONObject();
        cacheObj.put("risks", risks);
        cacheObj.put("type", resp.getString("type"));
        cacheObj.put("carrier", resp.getString("carrier"));
        JSONObject loc = resp.getJSONObject("location");
        if (loc != null) {
            cacheObj.put("country", loc.getString("country"));
            cacheObj.put("city", loc.getString("city"));
        }
        stringRedisTemplate.opsForValue().set(
                String.format(CACHE_KEY, ip), cacheObj.toJSONString(), CACHE_TTL_SECONDS, TimeUnit.SECONDS);
    }

    /** 将缓存内容应用到 VO（兼容旧格式：CACHE_SAFE 或旧的 risks JSON array） */
    private void applyToVo(IpDiagnoseResultVO vo, String cached) {
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
        // 旧格式：纯 risks 数组
        List<String> risks = JSON.parseArray(cached, String.class);
        if (risks != null) {
            vo.setRisky(!risks.isEmpty());
            vo.setRisks(risks);
        }
    }

    // ─── 工具方法 ─────────────────────────────────────────────

    private List<String> extractRisks(JSONObject resp) {
        List<String> risks = new ArrayList<>();
        if (Boolean.TRUE.equals(resp.getBoolean("isProxy")))   risks.add("PROXY");
        if (Boolean.TRUE.equals(resp.getBoolean("isVpn")))     risks.add("VPN");
        if (Boolean.TRUE.equals(resp.getBoolean("isTor")))     risks.add("TOR");
        if (Boolean.TRUE.equals(resp.getBoolean("isAbuser")))  risks.add("ABUSER");
        if (Boolean.TRUE.equals(resp.getBoolean("isCrawler"))) risks.add("CRAWLER");
        return risks;
    }

    private String buildLocation(JSONObject resp) {
        JSONObject loc = resp.getJSONObject("location");
        if (loc == null) return "";
        return String.format("(%s %s)", loc.getString("country"), loc.getString("city"));
    }

    private boolean isPrivateIp(String ip) {
        return ip.startsWith("127.") || ip.startsWith("10.") || ip.startsWith("192.168.")
                || ip.startsWith("172.16.") || ip.startsWith("172.17.") || ip.startsWith("172.18.")
                || ip.startsWith("172.19.") || ip.startsWith("172.2")   || ip.startsWith("172.30.")
                || ip.startsWith("172.31.") || ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1");
    }
}
