package cn.iocoder.yudao.module.custom.service.milestone;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.custom.controller.admin.milestone.vo.*;
import cn.iocoder.yudao.module.custom.dal.dataobject.milestone.MilestoneDO;

import javax.validation.Valid;

public interface MilestoneService {

    /**
     * 创建节点
     */
    Long createMilestone(@Valid MilestoneCreateReqVO createReqVO);

    /**
     * 更新节点
     */
    void updateMilestone(@Valid MilestoneUpdateReqVO updateReqVO);

    /**
     * 删除节点
     */
    void deleteMilestone(Long id);

    /**
     * 获取节点详情
     */
    MilestoneDO getMilestone(Long id);

    /**
     * 获得节点分页
     */
    PageResult<MilestoneDO> getMilestonePage(MilestonePageReqVO pageReqVO);

    /**
     * 获取看板概览汇总
     */
    MilestoneSummaryRespVO getMilestoneSummary();

    /**
     * 自动探测与同步域名SSL证书到期时间节点
     */
    String syncCertMilestones();
}
