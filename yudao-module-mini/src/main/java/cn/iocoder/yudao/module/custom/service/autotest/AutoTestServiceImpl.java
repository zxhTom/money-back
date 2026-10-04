package cn.iocoder.yudao.module.custom.service.autotest;

import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractSaveReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.custom.vo.ContractPayOrderCreateReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.custom.dal.mysql.contract.ContractMapper;
import cn.iocoder.yudao.module.custom.service.contract.ContractService;
import cn.iocoder.yudao.module.custom.service.custom.CustomDefineService;
import cn.iocoder.yudao.module.custom.service.face.baidu.BaiduFaceAuthService;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import cn.iocoder.yudao.module.pay.api.notify.dto.PayOrderNotifyReqDTO;
import cn.iocoder.yudao.module.pay.service.order.PayOrderService;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderSubmitReqVO;
import cn.iocoder.yudao.module.custom.dal.mysql.custom.CustomDefineMapper;
import cn.iocoder.yudao.module.pay.dal.mysql.demo.PayDemoOrderMapper;
import cn.iocoder.yudao.module.pay.dal.dataobject.demo.PayDemoOrderDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
public class AutoTestServiceImpl implements AutoTestService {

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

    @Override
    public boolean hasAdminRole() {
        return true; 
    }

    @Override
    public boolean runFullTest() throws Exception {
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
        Long demoOrderId = customDefineService.createDemoOrder(userA.getId(), orderCreateReqVO);
        log.info("创建 Demo 订单成功, demoOrderId={}", demoOrderId);

        PayDemoOrderDO payDemoOrderDO = payDemoOrderMapper.selectById(demoOrderId);
        if (payDemoOrderDO != null && payDemoOrderDO.getPayOrderId() != null) {
            Long payOrderId = payDemoOrderDO.getPayOrderId();
            PayOrderNotifyReqDTO notifyReqDTO = new PayOrderNotifyReqDTO();
            notifyReqDTO.setPayOrderId(payOrderId);
            notifyReqDTO.setChannelOrderNo("MOCK-O-" + payOrderId);
            customDefineService.updateContractConfirmedStatus(notifyReqDTO);
            log.info("模拟支付回调并确认合同成功, payOrderId={}", payOrderId);
        } else {
            log.error("未找到 PayOrderId，无法执行确认。");
            return false;
        }

        return true;
    }
}
