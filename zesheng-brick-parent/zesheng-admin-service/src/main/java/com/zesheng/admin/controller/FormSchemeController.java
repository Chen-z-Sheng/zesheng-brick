package com.zesheng.admin.controller;

import com.zesheng.admin.model.request.FormSchemePageRequest;
import com.zesheng.admin.model.request.FormSchemeSaveRequest;
import com.zesheng.admin.model.request.FormSchemeUpdateRequest;
import com.zesheng.admin.model.response.FormSchemeListResponse;
import com.zesheng.admin.model.response.FormSchemePageResponse;
import com.zesheng.admin.model.response.FormSchemeSaveResponse;
import com.zesheng.admin.model.response.FormSchemeUpdateResponse;
import com.zesheng.admin.model.response.FormSchemeVo;
import com.zesheng.admin.service.IFormSchemeService;
import com.zesheng.common.request.BatchDeleteRequest;
import com.zesheng.common.response.BatchDeleteResponse;
import com.zesheng.common.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("form-schemes")
@Tag(name = "管理端-方案模块", description = "管理端-方案相关模块")
@RequiredArgsConstructor
public class FormSchemeController {
    private final IFormSchemeService formSchemeService;

    /**
     * 新增方案
     *
     * @param formSchemeSaveRequest 新增请求
     * @return 新增结果
     */
    @PostMapping
    @Operation(summary = "新增方案")
    @PreAuthorize("hasAuthority('admin:form-scheme:add')")
    public R<FormSchemeSaveResponse> add(@Validated @RequestBody FormSchemeSaveRequest formSchemeSaveRequest) {
        return formSchemeService.save(formSchemeSaveRequest);
    }

    /**
     * 删除方案
     *
     * @param id 方案id列表
     * @return 删除数量
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除单个方案")
    @PreAuthorize("hasAuthority('admin:form-scheme:delete')")
    public R<Integer> delete(@PathVariable Long id) {
        return formSchemeService.delete(id);
    }


    /**
     * 批量删除方案
     *
     * @param req 方案id列表
     * @return 删除数量
     */
    @PostMapping("/delete")
    @Operation(summary = "批量删除方案")
    @PreAuthorize("hasAuthority('admin:form-scheme:delete')")
    public R<BatchDeleteResponse> batchDelete(@Validated @RequestBody BatchDeleteRequest req) {
        return formSchemeService.batchDelete(req);
    }

    /**
     * 修改方案
     *
     * @param id 方案id
     * @param formSchemeUpdateRequest 更新请求
     * @return 更新结果
     */
    @PatchMapping("/{id}")
    @Operation(summary = "更新方案")
    @PreAuthorize("hasAuthority('admin:form-scheme:update')")
    public R<FormSchemeUpdateResponse> update(
            @PathVariable("id") Long id,
            @Validated @RequestBody FormSchemeUpdateRequest formSchemeUpdateRequest
    ) {
        return formSchemeService.update(id, formSchemeUpdateRequest);
    }

    /**
     * 分页查询方案
     *
     * @param formSchemePageRequest 分页查询请求参数
     * @return 分页查询结果
     */
    @GetMapping("page")
    @Operation(summary = "分页查询方案")
    @PreAuthorize("hasAuthority('admin:form-scheme:list')")
    public R<FormSchemePageResponse> page(@Validated FormSchemePageRequest formSchemePageRequest) {
        return formSchemeService.page(formSchemePageRequest);
    }

    /**
     * 查询方案列表
     *
     * @return 方案列表
     */
    @GetMapping
    @Operation(summary = "查询方案列表")
    @PreAuthorize("hasAuthority('admin:form-scheme:list')")
    public R<List<FormSchemeListResponse>> list() {
        return formSchemeService.list();
    }

    /**
     * 查询方案详情
     *
     * @param id 方案id
     * @return 方案详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询方案详情")
    @PreAuthorize("hasAuthority('admin:form-scheme:list')")
    public R<FormSchemeVo> info(@PathVariable Long id) {
        return formSchemeService.info(id);
    }
}
