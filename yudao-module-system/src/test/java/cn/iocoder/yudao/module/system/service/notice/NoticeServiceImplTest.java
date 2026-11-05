package cn.iocoder.yudao.module.system.service.notice;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.system.controller.admin.notice.vo.NoticePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.notice.vo.NoticeSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.notice.NoticeDO;
import cn.iocoder.yudao.module.system.dal.mysql.notice.NoticeMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.NOTICE_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;

@Import(NoticeServiceImpl.class)
class NoticeServiceImplTest extends BaseDbUnitTest {

    @Resource
    private NoticeServiceImpl noticeService;

    @Resource
    private NoticeMapper noticeMapper;

    @Test
    public void testGetNoticePage_success() {
        // 插入前置数据
        NoticeDO dbNotice = randomPojo(NoticeDO.class, o -> {
            o.setTitle("尼古拉斯赵四来啦！");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        noticeMapper.insert(dbNotice);
        // 测试 title 不匹配
        noticeMapper.insert(cloneIgnoreId(dbNotice, o -> o.setTitle("尼古拉斯凯奇也来啦！")));
        // 测试 status 不匹配
        noticeMapper.insert(cloneIgnoreId(dbNotice, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        // 准备参数
        NoticePageReqVO reqVO = new NoticePageReqVO();
        reqVO.setTitle("尼古拉斯赵四来啦！");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        // 调用
        PageResult<NoticeDO> pageResult = noticeService.getNoticePage(reqVO);
        // 验证查询结果经过筛选
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbNotice, pageResult.getList().get(0));
    }

    @Test
    public void testGetNotice_success() {
        // 插入前置数据
        NoticeDO dbNotice = randomPojo(NoticeDO.class);
        noticeMapper.insert(dbNotice);

        // 查询
        NoticeDO notice = noticeService.getNotice(dbNotice.getId());

        // 验证插入与读取对象是否一致
        assertNotNull(notice);
        assertPojoEquals(dbNotice, notice);
    }

    @Test
    public void testCreateNotice_success() {
        // 准备参数
        NoticeSaveReqVO reqVO = randomPojo(NoticeSaveReqVO.class)
                .setId(null); // 避免 id 被赋值
        reqVO.setDisplayStartTime(null);
        reqVO.setDisplayEndTime(null);
        reqVO.setMaintenanceStartTime(null);
        reqVO.setMaintenanceEndTime(null);
        reqVO.setClearMaintenanceWindow(null);

        // 调用
        Long noticeId = noticeService.createNotice(reqVO);
        // 校验插入属性是否正确
        assertNotNull(noticeId);
        NoticeDO notice = noticeMapper.selectById(noticeId);
        assertPojoEquals(reqVO, notice, "id");
    }

    @Test
    public void testUpdateNotice_success() {
        // 插入前置数据
        NoticeDO dbNoticeDO = randomPojo(NoticeDO.class, notice -> {
            notice.setDisplayStartTime(null);
            notice.setDisplayEndTime(null);
            notice.setMaintenanceStartTime(null);
            notice.setMaintenanceEndTime(null);
        });
        noticeMapper.insert(dbNoticeDO);

        // 准备更新参数
        NoticeSaveReqVO reqVO = randomPojo(NoticeSaveReqVO.class, o -> o.setId(dbNoticeDO.getId()));
        reqVO.setDisplayStartTime(null);
        reqVO.setDisplayEndTime(null);
        reqVO.setMaintenanceStartTime(null);
        reqVO.setMaintenanceEndTime(null);
        reqVO.setClearMaintenanceWindow(null);

        // 更新
        noticeService.updateNotice(reqVO);
        // 检验是否更新成功
        NoticeDO notice = noticeMapper.selectById(reqVO.getId());
        assertPojoEquals(reqVO, notice);
    }

    @Test
    public void testDeleteNotice_success() {
        // 插入前置数据
        NoticeDO dbNotice = randomPojo(NoticeDO.class);
        noticeMapper.insert(dbNotice);

        // 删除
        noticeService.deleteNotice(dbNotice.getId());

        // 检查是否删除成功
        assertNull(noticeMapper.selectById(dbNotice.getId()));
    }

    @Test
    public void testValidateNoticeExists_success() {
        // 插入前置数据
        NoticeDO dbNotice = randomPojo(NoticeDO.class);
        noticeMapper.insert(dbNotice);

        // 成功调用
        noticeService.validateNoticeExists(dbNotice.getId());
    }

    @Test
    public void testValidateNoticeExists_noExists() {
        assertServiceException(() ->
                noticeService.validateNoticeExists(randomLongId()), NOTICE_NOT_FOUND);
    }

    @Test
    public void testPushNoticeOnlyShowsEnabledPushedNotices() {
        NoticeDO pushed = randomPojo(NoticeDO.class, o -> {
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setPushTime(null);
            o.setTargetUserId(null);
        });
        noticeMapper.insert(pushed);
        NoticeDO unpushed = randomPojo(NoticeDO.class, o -> {
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setPushTime(null);
        });
        noticeMapper.insert(unpushed);

        NoticeDO result = noticeService.pushNotice(pushed.getId());

        assertNotNull(result.getPushTime());
        assertEquals(1, noticeService.getRecentPushedNotices(10L).size());
        assertEquals(pushed.getId(), noticeService.getRecentPushedNotices(10L).get(0).getId());
        result.setStatus(CommonStatusEnum.DISABLE.getStatus());
        noticeMapper.updateById(result);
        assertTrue(noticeService.getRecentPushedNotices(10L).isEmpty());
    }

    @Test
    public void testTargetedNoticeVisibleOnlyToRecipient() {
        NoticeDO targeted = randomPojo(NoticeDO.class, notice -> {
            notice.setStatus(CommonStatusEnum.ENABLE.getStatus());
            notice.setTargetUserId(10L);
            notice.setPushTime(java.time.LocalDateTime.now());
            notice.setDisplayStartTime(null);
            notice.setDisplayEndTime(null);
        });
        noticeMapper.insert(targeted);

        assertEquals(targeted.getId(), noticeService.getRecentPushedNotices(10L).get(0).getId());
        assertTrue(noticeService.getRecentPushedNotices(11L).isEmpty());
    }

    @Test
    public void testActiveUpgradeVisibility() {
        NoticeDO upgrade = randomPojo(NoticeDO.class, notice -> {
            notice.setType(3);
            notice.setStatus(CommonStatusEnum.ENABLE.getStatus());
            notice.setTargetUserId(10L);
            notice.setPushTime(java.time.LocalDateTime.now());
            notice.setDisplayStartTime(null);
            notice.setDisplayEndTime(null);
        });
        noticeMapper.insert(upgrade);
        NoticeDO ordinary = randomPojo(NoticeDO.class, notice -> {
            notice.setType(2);
            notice.setStatus(CommonStatusEnum.ENABLE.getStatus());
            notice.setTargetUserId(null);
            notice.setPushTime(java.time.LocalDateTime.now());
        });
        noticeMapper.insert(ordinary);

        assertEquals(upgrade.getId(), noticeService.getActiveUpgradeNotices(10L).get(0).getId());
        assertTrue(noticeService.getActiveUpgradeNotices(11L).isEmpty());
        upgrade.setStatus(CommonStatusEnum.DISABLE.getStatus());
        noticeMapper.updateById(upgrade);
        assertTrue(noticeService.getActiveUpgradeNotices(10L).isEmpty());
    }

    @Test
    public void testUpgradeDisplayDeadlineAndLegacyExpiry() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        NoticeDO active = randomPojo(NoticeDO.class, notice -> {
            notice.setType(3);
            notice.setStatus(0);
            notice.setTargetUserId(null);
            notice.setPushTime(now.minusHours(2));
            notice.setDisplayStartTime(now.plusMinutes(10));
            notice.setDisplayEndTime(now.plusHours(2));
        });
        noticeMapper.insert(active);
        NoticeDO expired = randomPojo(NoticeDO.class, notice -> {
            notice.setType(3);
            notice.setStatus(0);
            notice.setTargetUserId(null);
            notice.setPushTime(now.minusHours(2));
            notice.setDisplayStartTime(now.minusHours(3));
            notice.setDisplayEndTime(now.minusMinutes(1));
        });
        noticeMapper.insert(expired);
        NoticeDO old = randomPojo(NoticeDO.class, notice -> {
            notice.setType(3);
            notice.setStatus(0);
            notice.setTargetUserId(null);
            notice.setPushTime(now.minusHours(25));
            notice.setDisplayStartTime(null);
            notice.setDisplayEndTime(null);
        });
        noticeMapper.insert(old);

        List<Long> ids = noticeService.getActiveUpgradeNotices(10L, true).stream()
                .map(NoticeDO::getId).collect(java.util.stream.Collectors.toList());
        assertTrue(ids.contains(active.getId())); // 提前返回，客户端到时展示
        assertFalse(ids.contains(expired.getId()));
        assertFalse(ids.contains(old.getId()));
        assertFalse(noticeService.getActiveUpgradeNotices(10L).stream()
                .anyMatch(notice -> notice.getId().equals(active.getId()))); // 旧版接口不提前展示
    }

    @Test
    public void testOldAdminUpdateKeepsScheduleAndNewAdminCanClearMaintenance() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now().withNano(0);
        NoticeDO existing = randomPojo(NoticeDO.class, notice -> {
            notice.setType(3);
            notice.setDisplayStartTime(now);
            notice.setDisplayEndTime(now.plusDays(1));
            notice.setMaintenanceStartTime(now.plusHours(1));
            notice.setMaintenanceEndTime(now.plusHours(2));
        });
        noticeMapper.insert(existing);
        NoticeSaveReqVO oldRequest = new NoticeSaveReqVO();
        oldRequest.setId(existing.getId());
        oldRequest.setTitle("旧版修改标题");
        noticeService.updateNotice(oldRequest);
        NoticeDO afterOld = noticeMapper.selectById(existing.getId());
        assertEquals(existing.getDisplayEndTime(), afterOld.getDisplayEndTime());
        assertEquals(existing.getMaintenanceStartTime(), afterOld.getMaintenanceStartTime());

        NoticeSaveReqVO clearRequest = new NoticeSaveReqVO();
        clearRequest.setId(existing.getId());
        clearRequest.setClearMaintenanceWindow(true);
        noticeService.updateNotice(clearRequest);
        NoticeDO afterClear = noticeMapper.selectById(existing.getId());
        assertNull(afterClear.getMaintenanceStartTime());
        assertNull(afterClear.getMaintenanceEndTime());
        assertEquals(existing.getDisplayEndTime(), afterClear.getDisplayEndTime());
    }

}
