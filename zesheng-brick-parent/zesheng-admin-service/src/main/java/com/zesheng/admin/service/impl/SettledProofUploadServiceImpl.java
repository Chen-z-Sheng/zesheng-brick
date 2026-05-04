package com.zesheng.admin.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.zesheng.common.config.OssConfig;
import com.zesheng.common.enums.ResultCodeEnum;
import com.zesheng.common.exception.BizException;
import com.zesheng.admin.service.ISettledProofUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import net.coobird.thumbnailator.Thumbnails;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

/**
 * 回款凭证上传：settled-proof/consolidated|market/年/月/随机名.png，大图自动压缩
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettledProofUploadServiceImpl implements ISettledProofUploadService {

    private static final String PREFIX = "settled-proof";
    private static final long COMPRESS_THRESHOLD_BYTES = 2 * 1024 * 1024L;
    private static final int MAX_WIDTH = 1920;
    private static final double QUALITY = 0.85;

    private final OssConfig ossConfig;

    @Override
    public String upload(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        String ext = getExtension(file.getOriginalFilename());
        if (!isAllowedImage(ext)) {
            throw new IllegalArgumentException("仅支持图片：jpg、jpeg、png、webp、gif");
        }
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + (ext != null ? ext : "png");
        String objectKey = buildObjectKey(PREFIX + "/" + subDir, year, month, fileName);

        byte[] data;
        String contentType;
        try {
            if (file.getSize() > COMPRESS_THRESHOLD_BYTES) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Thumbnails.of(file.getInputStream())
                        .size(MAX_WIDTH, MAX_WIDTH)
                        .outputQuality(QUALITY)
                        .outputFormat("jpg")
                        .toOutputStream(out);
                data = out.toByteArray();
                contentType = "image/jpeg";
            } else {
                data = file.getBytes();
                contentType = file.getContentType();
                if (!StringUtils.hasText(contentType)) {
                    contentType = "image/png";
                }
            }
        } catch (IOException e) {
            log.error("读取或压缩图片失败", e);
            throw new BizException(ResultCodeEnum.THIRD_PARTY_ERROR, "图片处理失败: " + e.getMessage(), e);
        }

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

    // 回款凭证不使用全局 dir-prefix，路径为 settled-proof/consolidated|market/年/月/文件名
    private String buildObjectKey(String baseDir, int year, int month, String fileName) {
        return baseDir + "/" + year + "/" + month + "/" + fileName;
    }

    private String buildFileUrl(String objectKey) {
        String host = ossConfig.getBucketName() + "."
                + (ossConfig.getEndpoint() != null ? ossConfig.getEndpoint().replace("https://", "").replace("http://", "") : "");
        return "https://" + host + "/" + objectKey;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return null;
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private boolean isAllowedImage(String ext) {
        if (ext == null) return false;
        return Arrays.asList("jpg", "jpeg", "png", "webp", "gif").contains(ext);
    }
}
