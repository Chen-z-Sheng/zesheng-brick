package com.zesheng.client.controller;

import com.zesheng.client.entity.Announcement;
import com.zesheng.client.service.IAnnouncementService;
import com.zesheng.common.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序端-公告
 */
@Tag(name = "小程序-公告", description = "登录后获取需弹窗公告、提交忽略")
@RestController
@RequestMapping("/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final IAnnouncementService announcementService;

    /**
     * 获取当前用户需要弹窗展示的启用公告
     * 建议：首页 onShow 或登录成功后调用，有则弹窗并展示“不再弹窗”选项
     */
    @GetMapping("/latest-to-show")
    @Operation(summary = "获取需弹窗的最新公告", description = "需登录；无则返回 data=null，有则弹窗展示并可选“不再弹窗”")
    public R<Announcement> getLatestToShow(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return R.success(null);
        }
        Announcement announcement = announcementService.getLatestToShow(userId);
        return R.success(announcement);
    }

    @GetMapping("/history")
    @Operation(summary = "公告历史", description = "返回未删除公告列表，供小程序历史页查看")
    public R<java.util.List<Announcement>> history() {
        return R.success(announcementService.listHistory());
    }

    /**
     * 用户点击“不再弹窗”时调用，写入忽略记录
     */
    @PostMapping("/ignore")
    @Operation(summary = "不再弹窗", description = "对指定公告写入忽略记录，之后 getLatestToShow 将不再返回该条")
    public R<Void> ignore(@Validated @RequestBody IgnoreRequest body, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return R.error("请先登录");
        }
        announcementService.ignore(userId, body.getAnnouncementId());
        return R.success();
    }

    private Long getUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId instanceof Long) {
            return (Long) userId;
        }
        return null;
    }

    @Data
    public static class IgnoreRequest {
        @NotNull(message = "公告ID不能为空")
        private Long announcementId;
    }
}
