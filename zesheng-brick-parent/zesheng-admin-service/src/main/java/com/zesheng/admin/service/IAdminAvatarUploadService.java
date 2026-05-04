package com.zesheng.admin.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 管理端头像上传至 OSS（固定对象键 avatars/admin/{userId}.jpg，覆盖更新）
 */
public interface IAdminAvatarUploadService {

    /**
     * 上传头像，返回可公网访问的 HTTPS URL
     *
     * @param file   图片文件
     * @param userId 当前管理员用户 ID
     * @return 文件 URL
     */
    String uploadAvatar(MultipartFile file, Long userId);
}
