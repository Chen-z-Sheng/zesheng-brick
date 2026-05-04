package com.zesheng.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zesheng.admin.entity.HelpFaq;
import com.zesheng.admin.model.request.HelpFaqSaveRequest;
import com.zesheng.admin.model.request.HelpFaqUpdateRequest;
import com.zesheng.admin.service.IHelpFaqService;
import com.zesheng.common.request.PageAndSortQueryRequest;
import com.zesheng.common.response.PageResult;
import com.zesheng.common.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端-帮助中心FAQ
 */
@RestController
@RequestMapping("help-faq")
@Tag(name = "管理端-帮助FAQ", description = "小程序帮助中心常见问题管理")
@RequiredArgsConstructor
public class HelpFaqController {

    private final IHelpFaqService helpFaqService;

    @GetMapping
    @Operation(summary = "FAQ列表")
    @PreAuthorize("hasAuthority('admin:help-faq:list')")
    public R<List<HelpFaq>> list() {
        return R.success(helpFaqService.list());
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询")
    @PreAuthorize("hasAuthority('admin:help-faq:list')")
    public R<PageResult<HelpFaq>> page(PageAndSortQueryRequest queryDto) {
        IPage<HelpFaq> iPage = helpFaqService.page(queryDto);
        return R.success(PageResult.success(iPage).getData());
    }

    @GetMapping("/{id}")
    @Operation(summary = "详情")
    @PreAuthorize("hasAuthority('admin:help-faq:list')")
    public R<HelpFaq> getById(@PathVariable Long id) {
        HelpFaq entity = helpFaqService.getById(id);
        if (entity == null) {
            return R.error("FAQ不存在");
        }
        return R.success(entity);
    }

    @PostMapping
    @Operation(summary = "新增")
    @PreAuthorize("hasAuthority('admin:help-faq:add')")
    public R<HelpFaq> save(@Validated @RequestBody HelpFaqSaveRequest request) {
        return helpFaqService.save(request);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "更新")
    @PreAuthorize("hasAuthority('admin:help-faq:update')")
    public R<HelpFaq> update(@PathVariable Long id, @Validated @RequestBody HelpFaqUpdateRequest request) {
        return helpFaqService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除")
    @PreAuthorize("hasAuthority('admin:help-faq:delete')")
    public R<Integer> deleteById(@PathVariable Long id) {
        return helpFaqService.deleteById(id);
    }
}
