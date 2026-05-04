package com.zesheng.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zesheng.admin.entity.Announcement;
import com.zesheng.admin.model.request.AnnouncementPageRequest;
import com.zesheng.admin.model.request.AnnouncementSaveRequest;
import com.zesheng.admin.model.request.AnnouncementUpdateRequest;
import com.zesheng.admin.service.IAnnouncementService;
import com.zesheng.admin.service.ISettledProofUploadService;
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
import java.util.Map;

/**
 * 管理端-公告管理
 */
@RestController
@RequestMapping("announcements")
@Tag(name = "管理端-公告", description = "小程序公告管理（富文本、生效/失效日期）")
@RequiredArgsConstructor
public class AnnouncementController {

    private static final String UPLOAD_SUB_DIR = "announcement";

    private final IAnnouncementService announcementService;
    private final ISettledProofUploadService settledProofUploadService;

    @GetMapping
    @Operation(summary = "公告列表")
    @PreAuthorize("hasAuthority('admin:announcement:list')")
    public R<List<Announcement>> list() {
        return R.success(announcementService.list());
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询")
    @PreAuthorize("hasAuthority('admin:announcement:list')")
    public R<PageResult<Announcement>> page(@Validated AnnouncementPageRequest queryDto) {
        IPage<Announcement> iPage = announcementService.page(queryDto);
        return R.success(PageResult.success(iPage).getData());
    }

    @GetMapping("/{id}")
    @Operation(summary = "详情")
    @PreAuthorize("hasAuthority('admin:announcement:list')")
    public R<Announcement> getById(@PathVariable Long id) {
        Announcement entity = announcementService.getById(id);
        if (entity == null) {
            return R.error("公告不存在");
        }
        return R.success(entity);
    }

    @PostMapping
    @Operation(summary = "新增")
    @PreAuthorize("hasAuthority('admin:announcement:add')")
    public R<Announcement> save(@Validated @RequestBody AnnouncementSaveRequest request) {
        return announcementService.save(request);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "修改")
    @PreAuthorize("hasAuthority('admin:announcement:update')")
    public R<Announcement> update(@PathVariable Long id, @Validated @RequestBody AnnouncementUpdateRequest request) {
        return announcementService.update(id, request);
    }

    @PostMapping("/{id}/enable")
    @Operation(summary = "启用公告")
    @PreAuthorize("hasAuthority('admin:announcement:update')")
    public R<Announcement> enable(@PathVariable Long id) {
        return announcementService.enable(id);
    }

    @PostMapping("/upload-image")
    @Operation(summary = "富文本内上传图片")
    @PreAuthorize("hasAuthority('admin:announcement:add')")
    public R<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return R.error("请选择图片");
        }
        String url = settledProofUploadService.upload(file, UPLOAD_SUB_DIR);
        return R.success(Map.of("url", url));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除")
    @PreAuthorize("hasAuthority('admin:announcement:delete')")
    public R<Integer> deleteById(@PathVariable Long id) {
        return announcementService.deleteById(id);
    }
}
