package cn.iocoder.yudao.module.custom.service.autotest;

import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractSaveReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.custom.vo.ContractPayOrderCreateReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.custom.dal.mysql.contract.ContractMapper;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.custom.service.contract.ContractService;
import cn.iocoder.yudao.module.custom.service.custom.CustomDefineService;
import cn.iocoder.yudao.module.custom.service.face.baidu.BaiduFaceAuthService;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import cn.iocoder.yudao.module.system.framework.idcard.IdCardCipherService;
import cn.iocoder.yudao.module.system.dal.mysql.oauth2.OAuth2AccessTokenMapper;
import cn.iocoder.yudao.module.pay.api.notify.dto.PayOrderNotifyReqDTO;
import cn.iocoder.yudao.module.pay.service.order.PayOrderService;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderSubmitReqVO;
import cn.iocoder.yudao.module.custom.dal.mysql.custom.CustomDefineMapper;
import cn.iocoder.yudao.module.pay.dal.mysql.demo.PayDemoOrderMapper;
import cn.iocoder.yudao.module.pay.dal.dataobject.demo.PayDemoOrderDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.Objects;
import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
@Service
public class AutoTestServiceImpl implements AutoTestService {
    @Resource private cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService securityFrameworkService;
    @Value("${custom.auto-test.enabled:false}") private boolean enabled;
    @Value("${server.domain:}") private String serverDomain;

    @Value("${custom.auto-test.user-a:zxhtom}")
    private String userAUsername;

    @Value("${custom.auto-test.user-b:wgl}")
    private String userBUsername;

    @Value("${custom.auto-test.login-password:123456}")
    private String loginPassword;

    @Value("${custom.auto-test.pay-password:123456}")
    private String payPassword;

    @Resource
    private AdminUserService adminUserService;

    @Resource
    private ContractService contractService;

    @Resource
    private CustomDefineService customDefineService;

    @Resource
    private BaiduFaceAuthService baiduFaceAuthService;

    @Resource
    private PayOrderService payOrderService;

    @Resource
    private ContractMapper contractMapper;

    @Resource
    private CustomDefineMapper customDefineMapper;

    @Resource
    private PayDemoOrderMapper payDemoOrderMapper;

    @Resource
    private IdCardCipherService idCardCipherService;

    @Resource
    private OAuth2AccessTokenMapper oauth2AccessTokenMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean hasAdminRole() {
        return securityFrameworkService.hasAnyRoles("super_admin", "tenant_admin");
    }

    @Override
    public Map<String, Boolean> runSmokeTest() {
        if (!hasAdminRole()) throw new org.springframework.security.access.AccessDeniedException("没有权限");
        Map<String, Boolean> checks = new LinkedHashMap<>();
        checks.put("管理员权限", true);
        AdminUserDO currentUser = null;
        try {
            currentUser = adminUserService.getUser(SecurityFrameworkUtils.getLoginUserId());
            checks.put("当前账号", currentUser != null);
        } catch (Exception e) {
            log.warn("自动检查：账号读取失败", e);
            checks.put("当前账号", false);
        }
        if (currentUser != null) {
            try {
                oauth2AccessTokenMapper.selectAdminTokenCountsByUserIds(java.util.Collections.singletonList(currentUser.getId()));
                checks.put("当前账号登录状态读取", true);
            } catch (Exception e) {
                log.warn("自动检查：当前账号登录状态读取失败", e);
                checks.put("当前账号登录状态读取", false);
            }
        }
        try {
            contractMapper.selectById(-1L);
            checks.put("合同数据库", true);
        } catch (Exception e) {
            log.warn("自动检查：合同数据库读取失败", e);
            checks.put("合同数据库", false);
        }
        try {
            payDemoOrderMapper.selectById(-1L);
            checks.put("支付订单数据库", true);
        } catch (Exception e) {
            log.warn("自动检查：支付订单数据库读取失败", e);
            checks.put("支付订单数据库", false);
        }
        AdminUserDO userA = null;
        AdminUserDO userB = null;
        try {
            // 固定限定在获准测试的两个账号；不读取其他用户资料。
            userA = adminUserService.getUserByUsername("zxhtom");
            userB = adminUserService.getUserByUsername("汪桂林");
            checks.put("指定测试账号", currentUser != null && userA != null && userB != null
                    && !userA.getId().equals(userB.getId())
                    && Objects.equals(currentUser.getTenantId(), userA.getTenantId())
                    && Objects.equals(userA.getTenantId(), userB.getTenantId()));
        } catch (Exception e) {
            log.warn("自动检查：指定测试账号读取失败", e);
            checks.put("指定测试账号", false);
        }
        if (Boolean.TRUE.equals(checks.get("指定测试账号"))) {
            final AdminUserDO a = userA;
            final AdminUserDO b = userB;
            try {
                String idA = idCardCipherService.resolveToPlain(a.getIdNo());
                String idB = idCardCipherService.resolveToPlain(b.getIdNo());
                if (org.apache.commons.lang3.StringUtils.isBlank(idA)
                        || org.apache.commons.lang3.StringUtils.isBlank(idB)) {
                    checks.put("双方合同往来（证件资料缺失）", false);
                } else {
                    Long count = contractMapper.selectCount(new LambdaQueryWrapperX<ContractDO>()
                            .and(w -> w.eq(ContractDO::getCreditorId, idA).eq(ContractDO::getIndebtedId, idB))
                            .or(w -> w.eq(ContractDO::getCreditorId, idB).eq(ContractDO::getIndebtedId, idA)));
                    checks.put("双方合同往来（" + (count == null ? 0 : count) + "份）", true);
                }
            } catch (Exception e) {
                log.warn("自动检查：双方合同往来读取失败", e);
                checks.put("双方合同往来读取", false);
            }
            try {
                Long count = payDemoOrderMapper.selectCount(new LambdaQueryWrapperX<PayDemoOrderDO>()
                        .in(PayDemoOrderDO::getUserId, Arrays.asList(a.getId(), b.getId())));
                checks.put("两人支付订单（" + (count == null ? 0 : count) + "笔）", true);
            } catch (Exception e) {
                log.warn("自动检查：两人支付订单读取失败", e);
                checks.put("两人支付订单读取", false);
            }
            try {
                oauth2AccessTokenMapper.selectAdminTokenCountsByUserIds(Arrays.asList(a.getId(), b.getId()));
                checks.put("两人登录状态读取", true);
            } catch (Exception e) {
                log.warn("自动检查：两人登录状态读取失败", e);
                checks.put("两人登录状态读取", false);
            }
        } else {
            checks.put("双方合同往来（账号未找到，未执行）", false);
            checks.put("两人支付订单（账号未找到，未执行）", false);
            checks.put("两人登录状态（账号未找到，未执行）", false);
        }
        try {
            stringRedisTemplate.hasKey("money:auto-test:read-only:health");
            checks.put("Redis 读取", true);
        } catch (Exception e) {
            log.warn("自动检查：Redis 读取失败", e);
            checks.put("Redis 读取", false);
        }
        return checks;
    }

    @Override
    public Map<String, Boolean> runFacePreflight() {
        if (!hasAdminRole()) throw new org.springframework.security.access.AccessDeniedException("没有权限");
        Map<String, Boolean> checks = new LinkedHashMap<>();
        boolean callbackConfigured = serverDomain != null
                && serverDomain.startsWith("https://brain.toms.chat/");
        checks.put("回调域名配置", callbackConfigured);
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL("https://brain.toms.chat/face/print/").openConnection();
            connection.setRequestMethod("HEAD");
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(4000);
            connection.setReadTimeout(4000);
            int status = connection.getResponseCode();
            checks.put("认证网页连通", status >= 200 && status < 400);
        } catch (Exception e) {
            log.warn("人脸预检：认证网页不可达，类型={}", e.getClass().getSimpleName());
            checks.put("认证网页连通", false);
        } finally {
            if (connection != null) connection.disconnect();
        }
        if (callbackConfigured) {
            try {
                String token = baiduFaceAuthService.getVerifyToken(
                        serverDomain + "/api/mini/callback?status=success",
                        serverDomain + "/api/mini/callback?status=failed");
                checks.put("百度认证令牌申请", org.apache.commons.lang3.StringUtils.isNotBlank(token));
            } catch (Exception e) {
                // Provider 异常可能包含认证参数；只记类型，不输出异常消息或令牌。
                log.warn("人脸预检：令牌申请失败，类型={}", e.getClass().getSimpleName());
                checks.put("百度认证令牌申请", false);
            }
        } else {
            checks.put("百度认证令牌申请（配置异常，未执行）", false);
        }
        return checks;
    }

    @Override
    public boolean runFullTest() throws Exception {
        if (!enabled || !hasAdminRole()) throw new org.springframework.security.access.AccessDeniedException("当前环境不允许执行自动化测试");
        log.info("开始执行自动化测试...");
        
        AdminUserDO userA = adminUserService.getUserByUsername(userAUsername);
        AdminUserDO userB = adminUserService.getUserByUsername(userBUsername);
        
        if (userA == null || userB == null) {
            log.error("测试账号未找到, userA={}, userB={}", userAUsername, userBUsername);
            return false;
        }

        ContractSaveReqVO createReqVO = new ContractSaveReqVO();
        createReqVO.setIndebtedName(userB.getRealname() != null ? userB.getRealname() : "王五");
        createReqVO.setIndebtedId(userB.getIdNo() != null ? userB.getIdNo() : "350211199001011234");
        createReqVO.setCreditorName(userA.getRealname() != null ? userA.getRealname() : "张三");
        createReqVO.setCreditorId(userA.getIdNo() != null ? userA.getIdNo() : "350211199001011235");
        createReqVO.setStatus(1);
        createReqVO.setSalary(new BigDecimal("1.00")); // 金额 1 元
        createReqVO.setTariff(new BigDecimal("0.00"));
        createReqVO.setStartDate(LocalDateTime.now());
        createReqVO.setEndDate(LocalDateTime.now().plusMonths(1));
        createReqVO.setReturnType("1");
        createReqVO.setReasonType("1");
        createReqVO.setDetailReason("自动化测试合同");
        createReqVO.setPassword(payPassword);
        
        Long contractId = contractService.createContract(createReqVO);
        log.info("模拟创建合同完成, contractId={}", contractId);

        try {
            String verifyToken = baiduFaceAuthService.getVerifyToken("http://success", "http://fail");
            log.info("获取百度人脸识别 Token 成功: {}", verifyToken);
        } catch (Exception e) {
            log.error("人脸识别测试失败", e);
        }

        ContractPayOrderCreateReqVO orderCreateReqVO = new ContractPayOrderCreateReqVO();
        orderCreateReqVO.setContractId(contractId);
        orderCreateReqVO.setPassword(payPassword);
        Long demoOrderId = customDefineService.createDemoOrder(userA.getId(), orderCreateReqVO);
        log.info("创建 Demo 订单成功, demoOrderId={}", demoOrderId);

        PayDemoOrderDO payDemoOrderDO = payDemoOrderMapper.selectById(demoOrderId);
        if (payDemoOrderDO != null && payDemoOrderDO.getPayOrderId() != null) {
            Long payOrderId = payDemoOrderDO.getPayOrderId();
            PayOrderNotifyReqDTO notifyReqDTO = new PayOrderNotifyReqDTO();
            notifyReqDTO.setPayOrderId(payOrderId);
            notifyReqDTO.setMerchantOrderId(payDemoOrderDO.getId().toString());
            customDefineService.updateContractConfirmedStatus(notifyReqDTO);
            log.info("模拟支付回调并确认合同成功, payOrderId={}", payOrderId);
        } else {
            log.error("未找到 PayOrderId，无法执行确认。");
            return false;
        }

        return true;
    }
}
