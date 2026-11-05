package cn.iocoder.yudao.module.system.controller.admin.notice;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.infra.api.websocket.WebSocketSenderApi;
import cn.iocoder.yudao.module.system.controller.admin.notice.vo.NoticeMiniRespVO;
import cn.iocoder.yudao.module.system.controller.admin.notice.vo.NoticePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.notice.vo.NoticeRespVO;
import cn.iocoder.yudao.module.system.controller.admin.notice.vo.NoticeSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.notice.NoticeDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.notice.NoticeService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 通知公告")
@RestController
@RequestMapping("/system/notice")
@Validated
public class NoticeController {

    @Resource
    private NoticeService noticeService;

    @Resource
    private WebSocketSenderApi webSocketSenderApi;

    @Resource
    private AdminUserService adminUserService;

    @PostMapping("/create")
    @Operation(summary = "创建通知公告")
    @PreAuthorize("@ss.hasPermission('system:notice:create')")
    public CommonResult<Long> createNotice(@Valid @RequestBody NoticeSaveReqVO createReqVO) {
        Long noticeId = noticeService.createNotice(createReqVO);
        return success(noticeId);
    }

    @PutMapping("/update")
    @Operation(summary = "修改通知公告")
    @PreAuthorize("@ss.hasPermission('system:notice:update')")
    public CommonResult<Boolean> updateNotice(@Valid @RequestBody NoticeSaveReqVO updateReqVO) {
        noticeService.updateNotice(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除通知公告")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:notice:delete')")
    public CommonResult<Boolean> deleteNotice(@RequestParam("id") Long id) {
        noticeService.deleteNotice(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除通知公告")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('system:notice:delete')")
    public CommonResult<Boolean> deleteNoticeList(@RequestParam("ids") List<Long> ids) {
        noticeService.deleteNoticeList(ids);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获取通知公告列表")
    @PreAuthorize("@ss.hasPermission('system:notice:query')")
    public CommonResult<PageResult<NoticeRespVO>> getNoticePage(@Validated NoticePageReqVO pageReqVO) {
        PageResult<NoticeDO> pageResult = noticeService.getNoticePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, NoticeRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得通知公告")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:notice:query')")
    public CommonResult<NoticeRespVO> getNotice(@RequestParam("id") Long id) {
        NoticeDO notice = noticeService.getNotice(id);
        return success(BeanUtils.toBean(notice, NoticeRespVO.class));
    }

    @PostMapping("/push")
    @Operation(summary = "推送通知公告", description = "只发送给 websocket 连接在线的用户")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:notice:update')")
    public CommonResult<Boolean> push(@RequestParam("id") Long id) {
        NoticeDO notice = noticeService.pushNotice(id);
        // 用户类型广播会跨租户；按当前租户的用户逐个发送，离线用户由小程序收件箱补看。
        if (notice.getTargetUserId() != null) {
            AdminUserDO target = adminUserService.getUser(notice.getTargetUserId());
            if (target != null && Integer.valueOf(0).equals(target.getStatus())) {
                webSocketSenderApi.sendObject(UserTypeEnum.ADMIN.getValue(), target.getId(), "notice-push", notice);
            }
        } else {
            for (AdminUserDO user : adminUserService.getUserListByStatus(0)) {
                webSocketSenderApi.sendObject(UserTypeEnum.ADMIN.getValue(), user.getId(), "notice-push", notice);
            }
        }
        return success(true);
    }

    @GetMapping("/mini/list")
    @Operation(summary = "小程序查看已推送通知公告")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<List<NoticeMiniRespVO>> getMiniNotices() {
        return success(toMiniNotices(noticeService.getRecentPushedNotices(getLoginUserId())));
    }

    @GetMapping("/mini/upgrades")
    @Operation(summary = "小程序查看当前系统升级公告")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<List<NoticeMiniRespVO>> getMiniUpgrades(
            @RequestParam(value = "includeScheduled", defaultValue = "false") boolean includeScheduled) {
        return success(toMiniNotices(noticeService.getActiveUpgradeNotices(getLoginUserId(), includeScheduled)));
    }

    private List<NoticeMiniRespVO> toMiniNotices(List<NoticeDO> source) {
        return source.stream().map(notice -> {
            NoticeMiniRespVO item = new NoticeMiniRespVO();
            item.setId(notice.getId());
            item.setTitle(notice.getTitle());
            item.setType(notice.getType());
            // 小程序使用纯文本组件展示，避免管理端富文本流入小程序渲染层。
            item.setContent(notice.getContent() == null ? "" : notice.getContent()
                    .replaceAll("(?s)<[^>]*>", " ")
                    .replace("&nbsp;", " ").replace("&amp;", "&")
                    .replace("&lt;", "<").replace("&gt;", ">")
                    .replaceAll("\\s+", " ").trim());
            item.setPushTime(notice.getPushTime());
            item.setDisplayStartTime(notice.getDisplayStartTime());
            item.setDisplayEndTime(notice.getDisplayEndTime());
            item.setMaintenanceStartTime(notice.getMaintenanceStartTime());
            item.setMaintenanceEndTime(notice.getMaintenanceEndTime());
            return item;
        }).collect(Collectors.toList());
    }

}
