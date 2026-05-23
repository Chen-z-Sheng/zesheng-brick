package com.zesheng.client.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zesheng.client.entity.FormSubmission;
import com.zesheng.client.service.IFormSubmissionService;
import com.zesheng.client.service.ILogisticsTraceService;
import com.zesheng.common.dto.logistics.LogisticsTraceVo;
import com.zesheng.client.service.IOssService;
import com.zesheng.common.response.PageResult;
import com.zesheng.common.response.R;
import com.zesheng.common.exception.AuthException;
import com.zesheng.common.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * C端-固结报单提交
 */
@Slf4j
@RestController
@RequestMapping("/form-submissions")
@Tag(name = "C端-表单提交", description = "固结报单提交/草稿")
@RequiredArgsConstructor
public class FormSubmissionController {

    private final IFormSubmissionService formSubmissionService;
    private final ILogisticsTraceService logisticsTraceService;
    private final IOssService ossService;
    private final JwtUtil jwtUtil;

    private static final String FORM_UPLOAD_DIR_PREFIX = "form_upload";

    @PostMapping("/upload-image")
    @Operation(summary = "固结报单图片上传", description = "上传到 form_upload/{yyyy-MM}/{schemeId}/{userId}/")
    public R<String> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("schemeId") Long schemeId,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        String yyyyMm = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String directory = FORM_UPLOAD_DIR_PREFIX + "/" + yyyyMm + "/" + schemeId + "/" + userId;
        String fileUrl = ossService.uploadFileWithMultipart(file, directory, userId, null, null);
        return R.success(fileUrl);
    }

    @PostMapping
    @Operation(summary = "提交/保存草稿")
    public R<Long> create(@Valid @RequestBody FormSubmissionCreateRequest body, HttpServletRequest request) {
        Long userId = getUserId(request);
        R<Long> result = formSubmissionService.save(
                userId,
                body.getSchemeId(),
                body.getQuantity(),
                body.getStatus(),
                body.getDataJson()
        );
        return result;
    }

    @GetMapping("/my-page")
    @Operation(summary = "我的固结报单分页", description = "statusTab: all|transit|storing|completed|exception")
    public R<PageResult<FormSubmission>> myPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String statusTab,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        IPage<FormSubmission> page = formSubmissionService.pageMy(userId, pageNum, pageSize, statusTab);
        return PageResult.success(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "我的固结报单详情")
    public R<FormSubmission> getMyById(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        FormSubmission entity = formSubmissionService.getMyById(userId, id);
        if (entity == null) {
            return R.error("记录不存在或无权查看");
        }
        return R.success(entity);
    }

    @GetMapping("/{id}/logistics-trace")
    @Operation(summary = "我的固结报单物流轨迹（快递100）")
    public R<List<LogisticsTraceVo>> logisticsTrace(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        return R.success(logisticsTraceService.traceFormSubmissionForUser(userId, id));
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        throw new AuthException("未授权访问");
    }

    @Data
    public static class FormSubmissionCreateRequest {
        @NotNull(message = "表单方案ID不能为空")
        private Long schemeId;
        @Min(value = 1, message = "数量至少为1")
        private Integer quantity = 1;
        /** 0=草稿 1=已提交；缺省时由服务端按已提交处理 */
        @Min(value = 0, message = "状态取值非法")
        @Max(value = 1, message = "状态取值非法")
        private Integer status = 1;
        private Map<String, Object> dataJson;
    }
}
