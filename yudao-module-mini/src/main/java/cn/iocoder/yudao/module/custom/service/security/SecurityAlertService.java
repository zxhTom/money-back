package cn.iocoder.yudao.module.custom.service.security;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.custom.controller.admin.security.vo.SecurityAlertHandleReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.security.vo.SecurityAlertPageReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.security.SecurityAlertDO;

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
