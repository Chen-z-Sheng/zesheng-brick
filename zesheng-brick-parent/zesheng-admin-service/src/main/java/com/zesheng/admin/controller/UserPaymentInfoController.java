package com.zesheng.admin.controller;

import com.zesheng.admin.entity.UserPaymentInfo;
import com.zesheng.admin.service.IUserPaymentInfoService;
import com.zesheng.common.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端-查询小程序用户收款信息（用于打款）
 */
@RestController
@RequestMapping("user-payment-info")
@Tag(name = "管理端-用户收款信息", description = "按用户ID查询收款账户与收款码")
@RequiredArgsConstructor
public class UserPaymentInfoController {

    private final IUserPaymentInfoService userPaymentInfoService;

    @GetMapping("/by-user/{userId}")
    @Operation(summary = "按用户ID查询收款信息")
    @PreAuthorize("hasAnyAuthority('admin:form-submission:list','admin:sell-order-submission:list')")
    public R<UserPaymentInfo> getByUserId(@PathVariable Long userId) {
        return R.success(userPaymentInfoService.getByUserId(userId));
    }
}
