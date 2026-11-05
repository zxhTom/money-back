package cn.iocoder.yudao.module.system.service.notice;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.controller.admin.notice.vo.NoticePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.notice.vo.NoticeSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.notice.NoticeDO;
import cn.iocoder.yudao.module.system.dal.mysql.notice.NoticeMapper;
import com.google.common.annotations.VisibleForTesting;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.NOTICE_NOT_FOUND;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;

/**
 * 通知公告 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class NoticeServiceImpl implements NoticeService {

    @Resource
    private NoticeMapper noticeMapper;

    @Override
    public Long createNotice(NoticeSaveReqVO createReqVO) {
        validateWindows(createReqVO);
        NoticeDO notice = BeanUtils.toBean(createReqVO, NoticeDO.class);
        noticeMapper.insert(notice);
        return notice.getId();
    }

    @Override
    public void updateNotice(NoticeSaveReqVO updateReqVO) {
        validateWindows(updateReqVO);
        // 校验是否存在
        validateNoticeExists(updateReqVO.getId());
        // 更新通知公告
        NoticeDO updateObj = BeanUtils.toBean(updateReqVO, NoticeDO.class);
        noticeMapper.updateById(updateObj);
        // 旧管理端不传新字段时保留排期；新版可显式清空可选的停机时段。
        if (Boolean.TRUE.equals(updateReqVO.getClearMaintenanceWindow())) {
            noticeMapper.update(null, new LambdaUpdateWrapper<NoticeDO>()
                    .eq(NoticeDO::getId, updateReqVO.getId())
                    .set(NoticeDO::getMaintenanceStartTime, null)
                    .set(NoticeDO::getMaintenanceEndTime, null));
        }
    }

    @Override
    public void deleteNotice(Long id) {
        // 校验是否存在
        validateNoticeExists(id);
        // 删除通知公告
        noticeMapper.deleteById(id);
    }

    @Override
    public void deleteNoticeList(List<Long> ids) {
        noticeMapper.deleteByIds(ids);
    }

    @Override
    public PageResult<NoticeDO> getNoticePage(NoticePageReqVO reqVO) {
        return noticeMapper.selectPage(reqVO);
    }

    @Override
    public NoticeDO getNotice(Long id) {
        return noticeMapper.selectById(id);
    }

    @Override
    public NoticeDO pushNotice(Long id) {
        NoticeDO notice = getNotice(id);
        if (notice == null) {
            throw exception(NOTICE_NOT_FOUND);
        }
        NoticeDO update = new NoticeDO();
        update.setId(id);
        update.setPushTime(LocalDateTime.now());
        noticeMapper.updateById(update);
        return noticeMapper.selectById(id);
    }

    @Override
    public List<NoticeDO> getRecentPushedNotices(Long userId) {
        return noticeMapper.selectRecentPushed(userId);
    }

    @Override
    public List<NoticeDO> getActiveUpgradeNotices(Long userId) {
        return noticeMapper.selectActiveUpgrades(userId);
    }

    @Override
    public List<NoticeDO> getActiveUpgradeNotices(Long userId, boolean includeScheduled) {
        return noticeMapper.selectActiveUpgrades(userId, includeScheduled);
    }

    @VisibleForTesting
    public void validateNoticeExists(Long id) {
        if (id == null) {
            return;
        }
        NoticeDO notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw exception(NOTICE_NOT_FOUND);
        }
    }

    private void validateWindows(NoticeSaveReqVO req) {
        if (req.getDisplayStartTime() != null || req.getDisplayEndTime() != null) {
            if (req.getDisplayStartTime() == null || req.getDisplayEndTime() == null
                    || !req.getDisplayEndTime().isAfter(req.getDisplayStartTime())) {
                throw exception0(BAD_REQUEST.getCode(), "横幅展示结束时间必须晚于开始时间");
            }
        }
        if (req.getMaintenanceStartTime() != null || req.getMaintenanceEndTime() != null) {
            if (req.getMaintenanceStartTime() == null || req.getMaintenanceEndTime() == null
                    || !req.getMaintenanceEndTime().isAfter(req.getMaintenanceStartTime())) {
                throw exception0(BAD_REQUEST.getCode(), "停机结束时间必须晚于开始时间");
            }
        }
    }

}
