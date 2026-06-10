package cn.iocoder.yudao.module.system.service.monitor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.DataAccessConfigPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.DataAccessConfigSaveReqVO;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.DataAccessLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.DataAccessConfigDO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.DataAccessLogDO;

import java.util.List;

public interface DataAccessMonitorService {

    /** AOP 调用：记录一次数据访问（异步执行，内含限额检查和告警） */
    void recordAccess(Long userId, String username, String module, String entityType,
                      String queryParams, List<Long> resultIds, int resultCount,
                      String requestUrl, String clientIp);

    // ─── 配置管理 ────────────────────────────────────────────
    Long createConfig(DataAccessConfigSaveReqVO req);
    void updateConfig(DataAccessConfigSaveReqVO req);
    void deleteConfig(Long id);
    DataAccessConfigDO getConfig(Long id);
    PageResult<DataAccessConfigDO> getConfigPage(DataAccessConfigPageReqVO req);

    // ─── 日志查询 ────────────────────────────────────────────
    PageResult<DataAccessLogDO> getLogPage(DataAccessLogPageReqVO req);

    Integer cleanDataAccessLog(Integer exceedDay, Integer deleteLimit);
}
