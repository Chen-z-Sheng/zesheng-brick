package com.zesheng.admin.controller;

import com.zesheng.admin.config.AdminPrincipal;
import com.zesheng.admin.service.IAdminAvatarUploadService;
import com.zesheng.common.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理端 OSS 上传（头像等）
 */
@RestController
@RequestMapping("oss")
@RequiredArgsConstructor
@Tag(name = "管理端-OSS", description = "管理端文件上传")
public class AdminOssController {

    private final IAdminAvatarUploadService adminAvatarUploadService;

    /**
     * 上传当前用户头像至 avatars/admin/{userId}.jpg（同键覆盖）
     */
    @PostMapping("/avatar")
    @Operation(summary = "上传管理员头像")
    @PreAuthorize("isAuthenticated()")
    public R<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = AdminPrincipal.getCurrentUserId();
        if (userId == null) {
            return R.error("未登录或会话已失效");
        }
        String fileUrl = adminAvatarUploadService.uploadAvatar(file, userId);
        Map<String, String> body = new HashMap<>(2);
        body.put("fileUrl", fileUrl);
        return R.success(body);
    }
}
