package com.zesheng.admin.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.zesheng.admin.service.IAdminAvatarUploadService;
import com.zesheng.common.config.OssConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * 管理端头像：OSS 固定键 avatars/admin/{userId}.jpg，再次上传即覆盖
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAvatarUploadServiceImpl implements IAdminAvatarUploadService {

    private static final String PREFIX = "avatars/admin";
    /** 上传原始文件最大 2MB */
    private static final long MAX_UPLOAD_BYTES = 2 * 1024 * 1024L;
    /** 存储前最长边限制 */
    private static final int MAX_EDGE = 512;
    private static final double OUTPUT_QUALITY = 0.88;

    private final OssConfig ossConfig;

    @Override
    public String uploadAvatar(MultipartFile file, Long userId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择图片文件");
        }
        if (userId == null) {
            throw new IllegalArgumentException("用户未登录");
        }
        if (file.getSize() > MAX_UPLOAD_BYTES) {
            throw new IllegalArgumentException("图片不能超过 2MB");
        }
        String ext = getExtension(file.getOriginalFilename());
        if (!isAllowedImage(ext)) {
            throw new IllegalArgumentException("仅支持 jpg、jpeg、png、webp、gif");
        }

        byte[] data;
        String contentType;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Thumbnails.of(file.getInputStream())
                    .size(MAX_EDGE, MAX_EDGE)
                    .outputQuality(OUTPUT_QUALITY)
                    .outputFormat("jpg")
                    .toOutputStream(out);
            data = out.toByteArray();
            contentType = "image/jpeg";
        } catch (IOException e) {
            log.error("头像压缩失败", e);
            throw new IllegalArgumentException("图片处理失败，请换一张图片重试");
        }

        String objectKey = PREFIX + "/" + userId + ".jpg";

        OSS client = new OSSClientBuilder().build(
                ossConfig.getEndpoint(),
                ossConfig.getAccessKeyId(),
                ossConfig.getAccessKeySecret()
        );
        try {
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(data.length);
            meta.setContentType(contentType);
            client.putObject(new PutObjectRequest(
                    ossConfig.getBucketName(),
                    objectKey,
                    new java.io.ByteArrayInputStream(data),
                    meta
            ));
            return buildFileUrl(objectKey);
        } finally {
            client.shutdown();
        }
    }

    private String buildFileUrl(String objectKey) {
        String host = ossConfig.getBucketName() + "."
                + (ossConfig.getEndpoint() != null
                ? ossConfig.getEndpoint().replace("https://", "").replace("http://", "")
                : "");
        return "https://" + host + "/" + objectKey;
    }

    private String getExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return null;
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private boolean isAllowedImage(String ext) {
        if (ext == null) {
            return false;
        }
        return Arrays.asList("jpg", "jpeg", "png", "webp", "gif").contains(ext);
    }
}
