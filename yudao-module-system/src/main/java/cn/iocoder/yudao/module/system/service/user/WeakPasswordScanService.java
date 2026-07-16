package cn.iocoder.yudao.module.system.service.user;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.UserRoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.permission.UserRoleMapper;
import cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper;
import cn.iocoder.yudao.module.system.service.permission.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 弱密码检测（仅超级管理员可用，结果只留内存、不落库）。
 *
 * 关键：密码是 bcrypt【加盐】存储，无法"预生成弱密码哈希再比对"（同一明文每次哈希都不同）。
 * 正确做法：逐用户取其存储哈希，用 passwordEncoder.matches(候选明文, 存储哈希) 判断
 *（bcrypt 会从存储哈希里取出该用户的盐再重算比对）。候选=常见弱口令 + 该用户相关派生。
 *
 * 隔离：结果只存内存（不写库/磁盘，避免"弱密码用户清单"落地泄漏），且只返回弱密码【类别】不返回明文/哈希。
 */
@Service
@Slf4j
public class WeakPasswordScanService {

    @Resource
    private AdminUserMapper adminUserMapper;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private RoleService roleService;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private AdminUserService adminUserService;
    @Resource
    @org.springframework.context.annotation.Lazy
    private cn.iocoder.yudao.module.system.service.oauth2.OAuth2TokenService oauth2TokenService;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "weak-pwd-scan");
        t.setDaemon(true);
        return t;
    });
    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile Map<String, Object> result = new LinkedHashMap<>();

    public boolean isSuperAdmin(Long userId) {
        if (userId == null) {
            return false;
        }
        List<Long> roleIds = userRoleMapper.selectListByUserId(userId).stream()
                .map(UserRoleDO::getRoleId).collect(Collectors.toList());
        return roleService.hasAnySuperAdmin(roleIds);
    }

    /** 触发扫描（已在扫描中则忽略）。异步后台执行，前端轮询 getResult。 */
    public synchronized void startScan() {
        if (running.get()) {
            return;
        }
        running.set(true);
        publish(0, 0, Collections.emptyList(), true);
        executor.submit(this::runScan);
    }

    public Map<String, Object> getResult() {
        Map<String, Object> r = this.result;
        if (r.isEmpty()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("running", running.get());
            m.put("scannedAt", null);
            m.put("totalUsers", 0);
            m.put("scanned", 0);
            m.put("weakCount", 0);
            m.put("weakUsers", Collections.emptyList());
            return m;
        }
        return r;
    }

    private void runScan() {
        try {
            // 后台线程无租户上下文；弱密码检测是全局管理操作，忽略租户读取全部用户
            List<AdminUserDO>[] holder = new List[]{Collections.emptyList()};
            cn.iocoder.yudao.framework.tenant.core.util.TenantUtils.executeIgnore(
                    () -> holder[0] = adminUserMapper.selectList());
            List<AdminUserDO> users = holder[0];
            int total = users.size(), scanned = 0;
            List<Map<String, Object>> weak = new ArrayList<>();
            for (AdminUserDO u : users) {
                scanned++;
                String type = detectWeak(u);
                if (type != null) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("userId", u.getId());
                    row.put("username", u.getUsername());
                    row.put("nickname", u.getNickname());
                    row.put("weakType", type);
                    weak.add(row);
                }
                if (scanned % 200 == 0) {
                    publish(total, scanned, weak, true);
                }
            }
            publish(total, total, weak, false);
            log.warn("[WeakPwdScan] 扫描完成：共 {} 用户，弱密码 {} 个", total, weak.size());
        } catch (Exception e) {
            log.error("[WeakPwdScan] 扫描失败", e);
        } finally {
            running.set(false);
        }
    }

    private void publish(int total, int scanned, List<Map<String, Object>> weak, boolean isRunning) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("running", isRunning);
        r.put("scannedAt", LocalDateTime.now().toString());
        r.put("totalUsers", total);
        r.put("scanned", scanned);
        r.put("weakCount", weak.size());
        r.put("weakUsers", new ArrayList<>(weak));
        this.result = r;
    }

    /**
     * 对选中用户强制改密（随机强口令）+ 踢下线。不返回/不落库新口令：新口令只在改密瞬间使用后即弃，
     * 用户改后即被踢下线，需通过"找回密码"自助重设——最大化安全，避免新口令泄漏。
     * updateUserPassword 内含"超管保护"，且本方法由超管上下文同步调用。
     */
    public int forceReset(List<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return 0;
        }
        int count = 0;
        for (Long id : userIds) {
            try {
                adminUserService.updateUserPassword(id, genStrongPassword());
                oauth2TokenService.removeAllTokensByUserId(id, UserTypeEnum.ADMIN.getValue());
                count++;
            } catch (Exception e) {
                log.warn("[WeakPwdScan] 强制改密失败 userId={}: {}", id, e.getMessage());
            }
        }
        log.warn("[WeakPwdScan] 强制改密+踢下线完成：请求 {} 个，成功 {} 个", userIds.size(), count);
        return count;
    }

    private static final String CU = "ABCDEFGHJKLMNPQRSTUVWXYZ", CL = "abcdefghijkmnpqrstuvwxyz",
            CD = "23456789", CS = "@#$%_-";

    private String genStrongPassword() {
        java.security.SecureRandom r = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder();
        sb.append(CU.charAt(r.nextInt(CU.length())));
        sb.append(CL.charAt(r.nextInt(CL.length())));
        sb.append(CD.charAt(r.nextInt(CD.length())));
        sb.append(CS.charAt(r.nextInt(CS.length())));
        String all = CU + CL + CD + CS;
        for (int i = 0; i < 12; i++) {
            sb.append(all.charAt(r.nextInt(all.length())));
        }
        return sb.toString();
    }

    private String detectWeak(AdminUserDO u) {
        String hash = u.getPassword();
        if (StrUtil.isBlank(hash)) {
            return null;
        }
        for (String[] c : candidates(u)) {
            try {
                if (passwordEncoder.matches(c[0], hash)) {
                    return c[1];
                }
            } catch (Exception ignore) {
                // 个别哈希格式异常忽略，不中断整体
            }
        }
        return null;
    }

    /** [明文, 类别]。默认口令与常见弱口令优先命中（多数弱账号在此早退出，性能友好）。 */
    private List<String[]> candidates(AdminUserDO u) {
        List<String[]> list = new ArrayList<>();
        // 注册默认口令（本系统未设密码时默认 123456）—— 最优先
        list.add(new String[]{"123456", "默认口令(123456)"});
        // 常见弱口令
        for (String p : COMMON) {
            list.add(new String[]{p, "常见弱口令"});
        }
        // 用户名相关
        String un = StrUtil.trimToEmpty(u.getUsername());
        if (un.length() >= 2) {
            for (String suf : new String[]{"", "123", "123456", "888", "@123", "2024", "2025"}) {
                list.add(new String[]{un + suf, "用户名相关"});
            }
        }
        // 手机号相关
        String mb = StrUtil.trimToEmpty(u.getMobile());
        if (mb.length() >= 6) {
            list.add(new String[]{mb, "手机号相关"});
            list.add(new String[]{mb.substring(mb.length() - 6), "手机号相关"});
            if (mb.length() >= 8) {
                list.add(new String[]{mb.substring(mb.length() - 8), "手机号相关"});
            }
        }
        return list;
    }

    private static final String[] COMMON = {
            "1234567", "12345678", "123456789", "1234567890", "12345", "1234",
            "111111", "000000", "666666", "888888", "999999", "222222", "121212",
            "123123", "112233", "123321", "654321", "147258369", "1234qwer",
            "abc123", "abcd1234", "a123456", "a1234567", "aa123456", "abc123456",
            "1qaz2wsx", "1q2w3e4r", "qwerty", "qwerty123", "qazwsx", "123qwe",
            "password", "passw0rd", "pass123", "p@ssw0rd", "P@ssw0rd", "Aa123456",
            "Aa@123456", "Abc@1234", "Admin@123", "Admin@2024", "Admin@2025",
            "admin", "admin123", "admin888", "administrator", "root", "root123",
            "manager", "test", "test123", "123456a", "123456aa",
            "5201314", "1314520", "woaini520", "woaini1314", "iloveyou",
            "88888888", "66668888", "11111111", "00000000"
    };
}
