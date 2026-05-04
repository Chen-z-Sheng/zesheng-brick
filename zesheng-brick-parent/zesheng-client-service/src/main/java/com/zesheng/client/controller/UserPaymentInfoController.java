package com.zesheng.client.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zesheng.client.entity.UserPaymentInfo;
import com.zesheng.client.model.request.UserPaymentInfoSaveRequest;
import com.zesheng.client.model.request.UserPaymentInfoUpdateRequest;
import com.zesheng.client.service.IUserPaymentInfoService;
import com.zesheng.common.request.PageAndSortQueryRequest;
import com.zesheng.common.response.PageResult;
import com.zesheng.common.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "用户端-收款信息管理", description = "用户收款信息增删改查")
@RestController
@RequestMapping("/payment-info")
public class UserPaymentInfoController {

    private final IUserPaymentInfoService userPaymentInfoService;

    public UserPaymentInfoController(IUserPaymentInfoService userPaymentInfoService) {
        this.userPaymentInfoService = userPaymentInfoService;
    }

    @Operation(summary = "分页查询收款信息列表")
    @GetMapping("/page")
    public R<PageResult<UserPaymentInfo>> page(@Valid PageAndSortQueryRequest queryDto) {
        IPage<UserPaymentInfo> iPage = userPaymentInfoService.page(queryDto);
        return PageResult.success(iPage);
    }

    @Operation(summary = "查询收款信息全量列表")
    @GetMapping
    public R<List<UserPaymentInfo>> list() {
        return R.success(userPaymentInfoService.list());
    }

    @Operation(summary = "按ID查询收款信息详情")
    @GetMapping("/{id}")
    public R<UserPaymentInfo> getById(@PathVariable Long id) {
        UserPaymentInfo entity = userPaymentInfoService.getById(id);
        if (entity == null) {
            return R.error(com.zesheng.common.enums.ResultCodeEnum.USER_PAYMENT_INFO_NOT_FOUND);
        }
        return R.success(entity);
    }

    @Operation(summary = "按用户ID查询收款信息")
    @GetMapping("/by-user/{userId}")
    public R<UserPaymentInfo> getByUserId(@PathVariable Long userId) {
        UserPaymentInfo entity = userPaymentInfoService.getByUserId(userId);
        if (entity == null) {
            return R.error(com.zesheng.common.enums.ResultCodeEnum.USER_PAYMENT_INFO_NOT_FOUND);
        }
        return R.success(entity);
    }

    @Operation(summary = "新增收款信息")
    @PostMapping
    public R<UserPaymentInfo> save(@Valid @RequestBody UserPaymentInfoSaveRequest request) {
        return userPaymentInfoService.save(request);
    }

    @Operation(summary = "更新收款信息")
    @PatchMapping("/{id}")
    public R<UserPaymentInfo> update(@PathVariable Long id, @Valid @RequestBody UserPaymentInfoUpdateRequest request) {
        return userPaymentInfoService.update(id, request);
    }

    @Operation(summary = "删除收款信息")
    @DeleteMapping("/{id}")
    public R<Integer> deleteById(@PathVariable Long id) {
        return userPaymentInfoService.deleteById(id);
    }
}
