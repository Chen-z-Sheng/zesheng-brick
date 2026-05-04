package com.zesheng.admin.controller;

import com.zesheng.admin.model.request.PasswordChangeRequest;
import com.zesheng.admin.model.request.SelfProfileUpdateRequest;
import com.zesheng.admin.model.request.UserPageRequest;
import com.zesheng.admin.model.request.UserSaveRequest;
import com.zesheng.admin.model.request.UserUpdateRequest;
import com.zesheng.admin.model.response.UserListResponse;
import com.zesheng.admin.model.response.UserPageResponse;
import com.zesheng.admin.model.response.UserSaveResponse;
import com.zesheng.admin.model.response.UserUpdateResponse;
import com.zesheng.admin.model.response.UserVo;
import com.zesheng.admin.service.IUserService;
import com.zesheng.common.request.BatchDeleteRequest;
import com.zesheng.common.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("user")
@Tag(name = "管理端-用户表模块", description = "管理端-用户表相关模块")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;

    /**
     * 新增用户表
     *
     * @param userSaveRequest 新增请求
     * @return 新增结果
     */
    @PostMapping
    @Operation(summary = "新增用户（仅超级管理员，权限码 admin）")
    @PreAuthorize("hasAuthority('admin')")
    public R<UserSaveResponse> add(@Validated @RequestBody UserSaveRequest userSaveRequest) {
        return userService.save(userSaveRequest);
    }

    /**
     * 删除用户表
     *
     * @param ids 用户表id列表
     * @return 删除数量
     */
    @PostMapping("/delete")
    @Operation(summary = "删除用户表")
    @PreAuthorize("hasAuthority('admin:user:delete')")
    public R<List<Long>> delete(@Validated @RequestBody BatchDeleteRequest request) {
        return userService.delete(request.getIds());
    }

    /**
     * 修改用户表
     *
     * @param id 用户表id
     * @param userUpdateRequest 更新请求
     * @return 更新结果
     */
    @PatchMapping("/{id}")
    @Operation(summary = "修改用户表")
    @PreAuthorize("hasAuthority('admin:user:update')")
    public R<UserUpdateResponse> update(
            @PathVariable("id") Long id,
            @Validated @RequestBody UserUpdateRequest userUpdateRequest
    ) {
        return userService.update(id, userUpdateRequest);
    }

    /**
     * 当前登录用户更新个人资料（无需 admin:user:update）
     */
    @PatchMapping("/self/profile")
    @Operation(summary = "当前用户更新资料")
    @PreAuthorize("isAuthenticated()")
    public R<UserUpdateResponse> updateSelfProfile(@Validated @RequestBody SelfProfileUpdateRequest request) {
        return userService.updateSelfProfile(request);
    }

    /**
     * 当前登录用户修改密码
     */
    @PostMapping("/self/password")
    @Operation(summary = "当前用户修改密码")
    @PreAuthorize("isAuthenticated()")
    public R<Void> changeOwnPassword(@Validated @RequestBody PasswordChangeRequest request) {
        return userService.changeOwnPassword(request);
    }

    /**
     * 分页查询用户表
     *
     * @param userPageRequest 分页查询请求参数
     * @return 分页查询结果
     */
    @GetMapping("page")
    @Operation(summary = "分页查询用户表")
    @PreAuthorize("hasAuthority('admin:user:list')")
    public R<UserPageResponse> page(@Validated UserPageRequest userPageRequest) {
        return userService.page(userPageRequest);
    }

    /**
     * 查询用户表列表
     *
     * @return 用户表列表
     */
    @GetMapping
    @Operation(summary = "查询用户表列表")
    @PreAuthorize("hasAuthority('admin:user:list')")
    public R<List<UserListResponse>> list() {
        return userService.list();
    }

    /**
     * 查询用户表详情
     *
     * @param id 用户表id
     * @return 用户表详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询用户表详情")
    @PreAuthorize("hasAuthority('admin:user:read')")
    public R<UserVo> info(@PathVariable("id") Long id) {
        return userService.info(id);
    }
}
