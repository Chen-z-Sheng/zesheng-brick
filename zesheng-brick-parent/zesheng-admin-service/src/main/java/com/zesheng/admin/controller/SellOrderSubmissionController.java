package com.zesheng.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zesheng.admin.entity.SellOrderSubmission;
import com.zesheng.admin.model.request.SellOrderSubmissionPageRequest;
import com.zesheng.admin.model.request.SellOrderSubmissionUpdateRequest;
import com.zesheng.admin.service.AdminLogisticsTraceService;
import com.zesheng.admin.service.ISellOrderSubmissionService;
import com.zesheng.admin.service.ISettledProofUploadService;
import com.zesheng.common.dto.logistics.LogisticsTraceVo;
import com.zesheng.common.response.PageResult;
import com.zesheng.common.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 管理端-行情报单提交记录
 */
@RestController
@RequestMapping("sell-order-submissions")
@Tag(name = "管理端-行情报单记录", description = "行情报单列表、详情、更新、回款凭证上传")
@RequiredArgsConstructor
public class SellOrderSubmissionController {

    private static final String SETTLED_PROOF_SUBDIR = "market";

    private final ISellOrderSubmissionService sellOrderSubmissionService;
    private final ISettledProofUploadService settledProofUploadService;
    private final AdminLogisticsTraceService adminLogisticsTraceService;

    @GetMapping
    @Operation(summary = "分页查询")
    @PreAuthorize("hasAuthority('admin:sell-order-submission:list')")
    public R<PageResult<SellOrderSubmission>> page(@Validated SellOrderSubmissionPageRequest request) {
        IPage<SellOrderSubmission> iPage = sellOrderSubmissionService.page(request);
        return PageResult.success(iPage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "详情")
    @PreAuthorize("hasAuthority('admin:sell-order-submission:list')")
    public R<SellOrderSubmission> getById(@PathVariable Long id) {
        SellOrderSubmission entity = sellOrderSubmissionService.getById(id);
        if (entity == null) {
            return R.error("记录不存在");
        }
        return R.success(entity);
    }

    @GetMapping("/{id}/logistics-trace")
    @Operation(summary = "物流轨迹（快递100）")
    @PreAuthorize("hasAuthority('admin:sell-order-submission:list')")
    public R<List<LogisticsTraceVo>> logisticsTrace(@PathVariable Long id) {
        return R.success(adminLogisticsTraceService.traceSellOrder(id));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "更新状态/备注/商品明细")
    @PreAuthorize("hasAuthority('admin:sell-order-submission:update')")
    public R<SellOrderSubmission> update(
            @PathVariable Long id,
            @Validated @RequestBody SellOrderSubmissionUpdateRequest request) {
        return sellOrderSubmissionService.update(id, request);
    }

    @PostMapping("/{id}/settled-proof")
    @Operation(summary = "上传回款凭证（可多张，大图自动压缩）")
    @PreAuthorize("hasAuthority('admin:sell-order-submission:update')")
    public R<SellOrderSubmission> uploadSettledProof(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return R.error("请选择图片");
        }
        String url = settledProofUploadService.upload(file, SETTLED_PROOF_SUBDIR);
        return sellOrderSubmissionService.appendSettledProofUrl(id, url);
    }

    @DeleteMapping("/{id}/settled-proof")
    @Operation(summary = "删除一条回款凭证，支持按索引或 URL 删除")
    @PreAuthorize("hasAuthority('admin:sell-order-submission:update')")
    public R<SellOrderSubmission> removeSettledProof(
            @PathVariable Long id,
            @RequestParam(required = false) Integer index,
            @RequestParam(required = false) String url) {
        return sellOrderSubmissionService.removeSettledProofUrl(id, url, index);
    }
}
