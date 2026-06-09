package cn.iocoder.yudao.module.system.service.monitor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.SecurityAlertHandleReqVO;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.SecurityAlertPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.SecurityAlertDO;

import java.util.List;
import java.util.Map;

public interface SecurityAlertService {

    void saveAsync(SecurityAlertDO alert);

    void saveAsync(String alertType, int severity, String sourceIp, Long userId,
                   String requestUrl, String requestMethod, String suspiciousContent, String alertMessage);

    PageResult<SecurityAlertDO> getPage(SecurityAlertPageReqVO reqVO);

    void handle(SecurityAlertHandleReqVO reqVO, Long operatorId);

    long countTodayUnhandled();

    List<Map<String, Object>> getTodayAlertTypeStats();

    List<Map<String, Object>> getTopAttackIps();
}
