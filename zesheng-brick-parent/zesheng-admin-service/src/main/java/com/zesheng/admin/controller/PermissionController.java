package com.zesheng.admin.controller;

import com.zesheng.common.request.BatchDeleteRequest;
import com.zesheng.common.response.BatchDeleteResponse;
import com.zesheng.common.response.R;
import com.zesheng.sys.model.request.*;
import com.zesheng.sys.model.response.*;
import com.zesheng.sys.service.IPermissionService;
import com.zesheng.sys.service.IRolePermissionService;
import com.zesheng.sys.service.IRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * admin端权限管理统一Controller
 * 整合角色、权限、角色权限关联的所有admin端接口
 * 接口前缀统一为/admin/sys，符合admin端前端请求规范
 */
@Slf4j
@RestController
@RequestMapping("sys")
@Tag(name = "管理端-权限管理模块", description = "管理端-角色/权限/角色权限关联相关接口")
@RequiredArgsConstructor
public class PermissionController {
    // 注入sys模块的核心服务
    private final IRoleService roleService;
    private final IRolePermissionService rolePermissionService;
    private final IPermissionService permissionService;

    // ========== 常量定义：拆分不同业务的名称，避免重复 ==========
    private static final String PERMISSION_BUSINESS_NAME = "系统端-权限表";
    private static final String ROLE_BUSINESS_NAME = "系统端-角色表";
    private static final String ROLE_PERMISSION_BUSINESS_NAME = "系统端-角色与权限关联表";

    // ========== 权限相关接口（路径：/admin/sys/permission） ==========
    @PostMapping("/permission")
    @Operation(summary = "新增系统端权限", description = "新增权限表数据，返回包含ID和创建时间的结果")
    @PreAuthorize("hasAuthority('sys:permission:add')")
    public R<PermissionSaveResponse> addPermission(@Validated @RequestBody PermissionSaveRequest saveRequest) {
        log.info("【新增{}】请求参数：{}", PERMISSION_BUSINESS_NAME, saveRequest);
        return permissionService.save(saveRequest);
    }

    @DeleteMapping("/permission/batch")
    @Operation(summary = "批量删除系统端权限", description = "仅删除存在的ID，返回实际删除数量")
    @PreAuthorize("hasAuthority('sys:permission:delete')")
    public R<Integer> deletePermissionBatch(@Validated @RequestBody BatchDeleteRequest request) {
        log.info("【批量删除{}】ID列表：{}", PERMISSION_BUSINESS_NAME, request.getIds());
        return permissionService.delete(request.getIds());
    }

    @PatchMapping("/permission/{id}")
    @Operation(summary = "修改系统端权限", description = "根据ID修改权限信息，返回包含更新时间的结果")
    @PreAuthorize("hasAuthority('sys:permission:update')")
    public R<PermissionUpdateResponse> updatePermission(
            @PathVariable Long id,
            @Parameter(description = "权限更新参数") @Validated @RequestBody PermissionUpdateRequest updateRequest
    ) {
        log.info("【修改{}】ID：{}，请求参数：{}", PERMISSION_BUSINESS_NAME, id, updateRequest);
        return permissionService.update(id, updateRequest);
    }

    @GetMapping("/permission/page")
    @Operation(summary = "分页查询系统端权限", description = "支持页码、页大小、排序参数，返回分页结果")
    @PreAuthorize("hasAuthority('sys:permission:list')")
    public R<PermissionPageResponse> pagePermission(@Validated PermissionPageRequest pageRequest) {
        log.info("【分页查询{}】请求参数：{}", PERMISSION_BUSINESS_NAME, pageRequest);
        return permissionService.page(pageRequest);
    }

    @GetMapping("/permission")
    @Operation(summary = "查询系统端权限列表（不分页）", description = "返回权限表完整列表")
    @PreAuthorize("hasAuthority('sys:permission:list')")
    public R<List<PermissionListResponse>> listPermission() {
        log.info("【查询{}列表】请求", PERMISSION_BUSINESS_NAME);
        return permissionService.list();
    }

    @GetMapping("/permission/{id}")
    @Operation(summary = "查询系统端权限详情", description = "根据ID返回权限完整信息")
    @PreAuthorize("hasAuthority('sys:permission:info')")
    public R<PermissionVo> infoPermission(@PathVariable Long id) {
        log.info("【查询{}详情】ID：{}", PERMISSION_BUSINESS_NAME, id);
        return permissionService.info(id);
    }

    /**
     * 根据用户ID查询权限列表
     * @param userId    用户ID
     * @return  R<List<PermissionListResponse>> 用户权限列表封装类
     */
    @GetMapping("/permission/user/{userId}")
    @Operation(summary = "根据用户ID查询权限列表", description = "查询指定用户拥有的所有权限列表，自动过滤禁用角色的权限；用户可查自己的权限，管理员可查任意用户")
    @PreAuthorize("hasAuthority('sys:permission:list') or (authentication.principal.userId != null and authentication.principal.userId == #userId)")
    public R<List<PermissionListResponse>> getPermissionsByUserId(@PathVariable Long userId) {
        log.info("【查询用户权限列表】用户ID：{}", userId);
        return permissionService.getPermissionsByUserId(userId);
    }

    // ========== 角色相关接口（路径：/admin/sys/role） ==========
    @PostMapping("/role")
    @Operation(summary = "新增系统端角色", description = "新增角色表数据，返回包含ID和创建时间的结果")
    @PreAuthorize("hasAuthority('sys:role:add')")
    public R<RoleSaveResponse> addRole(@Validated @RequestBody RoleSaveRequest saveRequest) {
        log.info("【新增{}】请求参数：{}", ROLE_BUSINESS_NAME, saveRequest);
        return roleService.save(saveRequest);
    }

    @DeleteMapping("/role/batch")
    @Operation(summary = "批量删除系统端角色", description = "仅删除存在的ID，返回实际删除数量")
    @PreAuthorize("hasAuthority('sys:role:delete')")
    public R<Integer> deleteRoleBatch(@Validated @RequestBody BatchDeleteRequest request) {
        log.info("【批量删除{}】ID列表：{}", ROLE_BUSINESS_NAME, request.getIds());
        return roleService.delete(request.getIds());
    }

    @PatchMapping("/role/{id}")
    @Operation(summary = "修改系统端角色", description = "根据ID修改角色信息，返回包含更新时间的结果")
    @PreAuthorize("hasAuthority('sys:role:update')")
    public R<RoleUpdateResponse> updateRole(
            @PathVariable Long id,
            @Parameter(description = "角色更新参数") @Validated @RequestBody RoleUpdateRequest updateRequest
    ) {
        log.info("【修改{}】ID：{}，请求参数：{}", ROLE_BUSINESS_NAME, id, updateRequest);
        return roleService.update(id, updateRequest);
    }

    @GetMapping("/role/page")
    @Operation(summary = "分页查询系统端角色", description = "支持页码、页大小、排序参数，返回分页结果")
    @PreAuthorize("hasAuthority('sys:role:list')")
    public R<RolePageResponse> pageRole(@Validated RolePageRequest pageRequest) {
        log.info("【分页查询{}】请求参数：{}", ROLE_BUSINESS_NAME, pageRequest);
        return roleService.page(pageRequest);
    }

    @GetMapping("/role")
    @Operation(summary = "查询系统端角色列表（不分页）", description = "返回角色表完整列表")
    @PreAuthorize("hasAuthority('sys:role:list')")
    public R<List<RoleListResponse>> listRole() {
        log.info("【查询{}列表】请求", ROLE_BUSINESS_NAME);
        return roleService.list();
    }

    @GetMapping("/role/{id}")
    @Operation(summary = "查询系统端角色详情", description = "根据ID返回角色完整信息")
    @PreAuthorize("hasAuthority('sys:role:info')")
    public R<RoleVo> infoRole(@PathVariable Long id) {
        log.info("【查询{}详情】ID：{}", ROLE_BUSINESS_NAME, id);
        return roleService.info(id);
    }

    // ========== 角色权限关联相关接口（路径：/admin/sys/role-permission） ==========
    @PostMapping("/role-permission")
    @Operation(summary = "新增角色权限关联", description = "新增角色与权限的关联关系，返回包含ID的结果")
    @PreAuthorize("hasAuthority('sys:role-permission:add')")
    public R<RolePermissionSaveResponse> addRolePermission(@Validated @RequestBody RolePermissionSaveRequest saveRequest) {
        log.info("【新增{}】请求参数：{}", ROLE_PERMISSION_BUSINESS_NAME, saveRequest);
        return rolePermissionService.save(saveRequest);
    }

    @DeleteMapping("/role-permission/{id}")
    @Operation(summary = "单个删除角色权限关联", description = "根据ID删除角色与权限的关联关系")
    @PreAuthorize("hasAuthority('sys:role-permission:delete')")
    public R<Void> deleteRolePermission(@Parameter(description = "关联关系ID") @PathVariable Long id) {
        log.info("【删除{}】ID：{}", ROLE_PERMISSION_BUSINESS_NAME, id);
        return rolePermissionService.delete(id);
    }

    @DeleteMapping("/role-permission/batch")
    @Operation(summary = "批量删除角色权限关联", description = "批量删除角色与权限的关联关系，返回删除详情")
    @PreAuthorize("hasAuthority('sys:role-permission:delete')")
    public R<BatchDeleteResponse> deleteRolePermissionBatch(@Validated @RequestBody BatchDeleteRequest request) {
        log.info("【批量删除{}】ID列表：{}", ROLE_PERMISSION_BUSINESS_NAME, request.getIds());
        return rolePermissionService.delete(request.getIds());
    }

    @PatchMapping("/role-permission/{id}")
    @Operation(summary = "修改角色权限关联", description = "根据ID修改角色与权限的关联关系")
    @PreAuthorize("hasAuthority('sys:role-permission:update')")
    public R<RolePermissionUpdateResponse> updateRolePermission(
            @PathVariable Long id,
            @Parameter(description = "角色权限关联更新参数") @Validated @RequestBody RolePermissionUpdateRequest updateRequest
    ) {
        log.info("【修改{}】ID：{}，请求参数：{}", ROLE_PERMISSION_BUSINESS_NAME, id, updateRequest);
        return rolePermissionService.update(id, updateRequest);
    }

    @GetMapping("/role-permission/page")
    @Operation(summary = "分页查询角色权限关联", description = "分页返回角色与权限的关联关系列表")
    @PreAuthorize("hasAuthority('sys:role-permission:list')")
    public R<RolePermissionPageResponse> pageRolePermission(@Validated RolePermissionPageRequest pageRequest) {
        log.info("【分页查询{}】请求参数：{}", ROLE_PERMISSION_BUSINESS_NAME, pageRequest);
        // 【TODO】补充admin端权限校验：checkAdminPermission("rolePermission:page")
        return rolePermissionService.page(pageRequest);
    }

    @GetMapping("/role-permission")
    @Operation(summary = "查询角色权限关联列表（不分页）", description = "返回角色与权限的关联关系完整列表")
    @PreAuthorize("hasAuthority('sys:role-permission:list')")
    public R<List<RolePermissionListResponse>> listRolePermission() {
        log.info("【查询{}列表】请求", ROLE_PERMISSION_BUSINESS_NAME);
        return rolePermissionService.list();
    }

    /**
     * 查询角色权限关联详情
     * @param id    关联关系ID
     * @return      包含关联关系完整信息的响应结果
     */
    @GetMapping("/role-permission/{id}")
    @Operation(summary = "查询角色权限关联详情", description = "根据ID返回角色与权限的关联关系完整信息")
    @PreAuthorize("hasAuthority('sys:role-permission:info')")
    public R<RolePermissionVo> infoRolePermission(@PathVariable Long id) {
        log.info("【查询{}详情】ID：{}", ROLE_PERMISSION_BUSINESS_NAME, id);
        return rolePermissionService.info(id);
    }
}