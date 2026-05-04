package com.zesheng.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zesheng.admin.config.AdminPrincipal;
import com.zesheng.admin.entity.UserFeedback;
import com.zesheng.admin.model.request.UserFeedbackPageRequest;
import com.zesheng.admin.model.request.UserFeedbackReplyRequest;
import com.zesheng.admin.service.IUserFeedbackService;
import com.zesheng.common.response.PageResult;
import com.zesheng.common.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端-用户问题反馈
 */
@RestController
@RequestMapping("user-feedback")
@Tag(name = "管理端-用户问题反馈", description = "小程序用户问题反馈查看")
@RequiredArgsConstructor
public class UserFeedbackController {

    private final IUserFeedbackService userFeedbackService;

    @GetMapping
    @Operation(summary = "分页查询")
    @PreAuthorize("hasAuthority('admin:user-feedback:list')")
    public R<PageResult<UserFeedback>> page(@Validated UserFeedbackPageRequest request) {
        IPage<UserFeedback> page = userFeedbackService.page(request);
        return PageResult.success(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "详情")
    @PreAuthorize("hasAuthority('admin:user-feedback:list')")
    public R<UserFeedback> detail(@PathVariable Long id) {
        UserFeedback entity = userFeedbackService.getById(id);
        if (entity == null) {
            return R.error("反馈记录不存在");
        }
        return R.success(entity);
    }

    @PatchMapping("/{id}/reply")
    @Operation(summary = "回复反馈")
    @PreAuthorize("hasAuthority('admin:user-feedback:reply')")
    public R<UserFeedback> reply(@PathVariable Long id, @Validated @RequestBody UserFeedbackReplyRequest request) {
        Long adminUserId = AdminPrincipal.getCurrentUserId();
        return userFeedbackService.reply(id, adminUserId, request.getReplyContent());
    }
}
