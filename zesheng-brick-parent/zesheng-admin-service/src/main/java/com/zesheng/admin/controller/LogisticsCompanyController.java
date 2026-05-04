package com.zesheng.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zesheng.admin.entity.LogisticsCompany;
import com.zesheng.admin.model.request.LogisticsCompanyPageRequest;
import com.zesheng.admin.model.request.LogisticsCompanySaveRequest;
import com.zesheng.admin.model.request.LogisticsCompanyUpdateRequest;
import com.zesheng.admin.service.ILogisticsCompanyService;
import com.zesheng.common.response.PageResult;
import com.zesheng.common.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 与 {@link com.zesheng.admin.controller.DeliveryAddressController} 同风格：context-path 已为 /api/admin，此处仅写资源路径。
 */
@Validated
@RestController
@RequestMapping("logistics-companies")
@Tag(name = "管理端-物流公司", description = "物流公司增删改查")
@RequiredArgsConstructor
public class LogisticsCompanyController {

    private final ILogisticsCompanyService logisticsCompanyService;

    @GetMapping("/page")
    @Operation(summary = "分页列表，name 可选模糊匹配名称")
    @PreAuthorize("hasAuthority('admin:logistics-company:list')")
    public R<PageResult<LogisticsCompany>> page(@Validated LogisticsCompanyPageRequest request) {
        IPage<LogisticsCompany> iPage = logisticsCompanyService.page(request);
        return PageResult.success(iPage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "详情")
    @PreAuthorize("hasAuthority('admin:logistics-company:list')")
    public R<LogisticsCompany> get(@PathVariable Long id) {
        return logisticsCompanyService.get(id);
    }

    @PostMapping
    @Operation(summary = "新增")
    @PreAuthorize("hasAuthority('admin:logistics-company:add')")
    public R<LogisticsCompany> save(@Validated @RequestBody LogisticsCompanySaveRequest request) {
        return logisticsCompanyService.save(request);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "修改")
    @PreAuthorize("hasAuthority('admin:logistics-company:update')")
    public R<LogisticsCompany> update(
            @PathVariable Long id,
            @Validated @RequestBody LogisticsCompanyUpdateRequest request) {
        return logisticsCompanyService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除")
    @PreAuthorize("hasAuthority('admin:logistics-company:delete')")
    public R<Integer> delete(@PathVariable Long id) {
        return logisticsCompanyService.delete(id);
    }
}
