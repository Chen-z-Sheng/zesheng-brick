package com.zesheng.client.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zesheng.client.entity.UserFeedback;
import com.zesheng.client.service.IOssService;
import com.zesheng.client.service.IUserFeedbackService;
import com.zesheng.common.response.PageResult;
import com.zesheng.common.response.R;
import com.zesheng.common.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * C端-问题反馈
 */
@Slf4j
@RestController
@RequestMapping("/feedback")
@Tag(name = "C端-问题反馈", description = "提交问题反馈、上传反馈图片")
@RequiredArgsConstructor
@Validated
public class UserFeedbackController {

    private static final String FEEDBACK_UPLOAD_DIR_PREFIX = "feedback";

    private final IUserFeedbackService userFeedbackService;
    private final IOssService ossService;
    private final JwtUtil jwtUtil;

    @PostMapping("/upload-image")
    @Operation(summary = "问题反馈图片上传")
    public R<String> uploadImage(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        Long userId = getUserId(request);
        String yyyyMm = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String directory = FEEDBACK_UPLOAD_DIR_PREFIX + "/" + yyyyMm + "/" + userId;
        String fileUrl = ossService.uploadFileWithMultipart(file, directory, userId, null, null);
        return R.success(fileUrl);
    }

    @PostMapping
    @Operation(summary = "提交问题反馈")
    public R<UserFeedback> submit(@Valid @RequestBody FeedbackSubmitRequest request, HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);
        UserFeedback feedback = userFeedbackService.submit(
                userId,
                request.getFeedbackType(),
                request.getContent(),
                request.getImageUrls()
        );
        return R.success(feedback);
    }

    @GetMapping("/my-page")
    @Operation(summary = "我的反馈分页")
    public R<PageResult<UserFeedback>> myPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        IPage<UserFeedback> page = userFeedbackService.pageMy(userId, pageNum, pageSize);
        return PageResult.success(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "我的反馈详情")
    public R<UserFeedback> detail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        UserFeedback feedback = userFeedbackService.getMyById(userId, id);
        if (feedback == null) {
            return R.error("反馈记录不存在");
        }
        return R.success(feedback);
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        throw new IllegalArgumentException("未授权访问");
    }

    @Data
    public static class FeedbackSubmitRequest {
        @NotBlank(message = "反馈类型不能为空")
        private String feedbackType;

        @NotBlank(message = "反馈内容不能为空")
        private String content;

        private List<String> imageUrls;
    }
}
