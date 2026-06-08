package cn.iocoder.yudao.module.custom.service.security;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.custom.controller.admin.security.vo.SecurityAlertHandleReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.security.vo.SecurityAlertPageReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.security.SecurityAlertDO;
import cn.iocoder.yudao.module.custom.dal.mysql.security.SecurityAlertMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SecurityAlertServiceImpl implements SecurityAlertService {

    @Resource
    private SecurityAlertMapper securityAlertMapper;

    @Override
    @Async
    public void saveAsync(SecurityAlertDO alert) {
        try {
            alert.setCreateTime(LocalDateTime.now());
            securityAlertMapper.insert(alert);
        } catch (Exception e) {
            log.error("[SecurityAlert] 告警写入失败", e);
        }
    }

    @Override
    @Async
    public void saveAsync(String alertType, int severity, String sourceIp, Long userId,
                          String requestUrl, String requestMethod, String suspiciousContent, String alertMessage) {
        SecurityAlertDO alert = new SecurityAlertDO();
        alert.setAlertType(alertType);
        alert.setSeverity(severity);
        alert.setSourceIp(sourceIp);
        alert.setUserId(userId);
        alert.setRequestUrl(requestUrl);
        alert.setRequestMethod(requestMethod);
        alert.setSuspiciousContent(truncate(suspiciousContent, 2000));
        alert.setAlertMessage(alertMessage);
        alert.setHandled(0);
        alert.setCreateTime(LocalDateTime.now());
        try {
            securityAlertMapper.insert(alert);
        } catch (Exception e) {
            log.error("[SecurityAlert] 告警写入失败", e);
        }
    }

    @Override
    public PageResult<SecurityAlertDO> getPage(SecurityAlertPageReqVO reqVO) {
        return securityAlertMapper.selectPage(reqVO);
    }

    @Override
    public void handle(SecurityAlertHandleReqVO reqVO, Long operatorId) {
        securityAlertMapper.update(null, new LambdaUpdateWrapper<SecurityAlertDO>()
                .eq(SecurityAlertDO::getId, reqVO.getId())
                .set(SecurityAlertDO::getHandled, reqVO.getHandled())
                .set(SecurityAlertDO::getHandleBy, operatorId)
                .set(SecurityAlertDO::getHandleTime, LocalDateTime.now())
                .set(SecurityAlertDO::getHandleRemark, reqVO.getHandleRemark()));
    }

    @Override
    public long countTodayUnhandled() {
        return securityAlertMapper.selectCount(new LambdaQueryWrapper<SecurityAlertDO>()
                .eq(SecurityAlertDO::getHandled, 0)
                .ge(SecurityAlertDO::getCreateTime, LocalDateTime.now().toLocalDate().atStartOfDay()));
    }

    @Override
    public List<Map<String, Object>> getTodayAlertTypeStats() {
        return securityAlertMapper.selectTodayAlertTypeStats();
    }

    @Override
    public List<Map<String, Object>> getTopAttackIps() {
        return securityAlertMapper.selectTopAttackIps();
    }

    private String truncate(String s, int max) {
        if (s == null || s.length() <= max) return s;
        return s.substring(0, max);
    }
}
