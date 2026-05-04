package com.zesheng.client.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.model.*;
import com.zesheng.client.service.IOssService;
import com.zesheng.common.config.OssConfig;
import com.zesheng.common.enums.ResultCodeEnum;
import com.zesheng.common.exception.BizException;
import com.zesheng.common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.aliyun.oss.HttpMethod;

/**
 * OSS文件上传服务实现
 * 支持简单上传、分片上传、断点续传、进度显示等功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OssServiceImpl implements IOssService {

    private final OssConfig ossConfig;
    private final RedisUtil redisUtil;

    private static final String REDIS_UPLOAD_PREFIX = "oss:upload:";
    private static final String REDIS_UPLOAD_PROGRESS_PREFIX = "oss:upload:progress:";

    /**
     * 获取OSS客户端实例
     */
    private OSS getOssClient() {
        CredentialsProvider credentialsProvider = new DefaultCredentialProvider(
                ossConfig.getAccessKeyId(),
                ossConfig.getAccessKeySecret()
        );
        return OSSClientBuilder.create()
                .endpoint(ossConfig.getEndpoint())
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Override
    public String uploadFile(MultipartFile file, String directory, Long userId) {
        return uploadFileWithMultipart(file, directory, userId, null, null);
    }

    @Override
    public String uploadFileWithUserId(MultipartFile file, String directory, Long userId) {
        try {
            validateFile(file);

            String extension = getFileExtension(file.getOriginalFilename());
            String fileName = userId + "." + extension;
            String objectKey = buildObjectKey(directory, fileName);
            OSS ossClient = getOssClient();

            long fileSize = file.getSize();

            if (fileSize <= ossConfig.getPartSize()) {
                log.info("文件较小，使用简单上传，文件大小: {}", fileSize);
                return simpleUpload(ossClient, file, objectKey, null);
            } else {
                log.info("文件较大，使用分片上传，文件大小: {}", fileSize);
                String uploadId = UUID.randomUUID().toString();
                return multipartUpload(ossClient, file, objectKey, uploadId, null);
            }
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BizException(ResultCodeEnum.THIRD_PARTY_ERROR, "文件上传失败: " + e.getMessage(), e);
        }
    }

    private static final String DIRECTORY_WECHAT_QRCODE = "wechat-qrcode";
    private static final String DIRECTORY_ALIPAY_QRCODE = "alipay-qrcodes";

    @Override
    public String uploadFileWithMultipart(
            MultipartFile file,
            String directory,
            Long userId,
            String uploadId,
            Consumer<Double> progressCallback) {

        try {
            validateFile(file);

            String fileName;
            if (DIRECTORY_WECHAT_QRCODE.equals(directory) || DIRECTORY_ALIPAY_QRCODE.equals(directory)) {
                String extension = getFileExtension(file.getOriginalFilename());
                fileName = userId + "." + extension;
            } else {
                fileName = generateFileName(file.getOriginalFilename());
            }
            String objectKey = buildObjectKey(directory, fileName);
            OSS ossClient = getOssClient();

            long fileSize = file.getSize();

            if (fileSize <= ossConfig.getPartSize()) {
                log.info("文件较小，使用简单上传，文件大小: {}", fileSize);
                return simpleUpload(ossClient, file, objectKey, progressCallback);
            } else {
                log.info("文件较大，使用分片上传，文件大小: {}", fileSize);
                if (uploadId == null) {
                    uploadId = UUID.randomUUID().toString();
                }
                return multipartUpload(ossClient, file, objectKey, uploadId, progressCallback);
            }
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BizException(ResultCodeEnum.THIRD_PARTY_ERROR, "文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 简单上传（适合小文件）
     */
    private String simpleUpload(
            OSS ossClient,
            MultipartFile file,
            String objectKey,
            Consumer<Double> progressCallback) throws IOException {

        long fileSize = file.getSize();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(fileSize);
        metadata.setContentType(file.getContentType());

        PutObjectRequest putObjectRequest = new PutObjectRequest(
                ossConfig.getBucketName(),
                objectKey,
                file.getInputStream(),
                metadata
        );

        ossClient.putObject(putObjectRequest);
        return buildFileUrl(objectKey);
    }

    /**
     * 分片上传（适合大文件，支持断点续传）
     */
    @SuppressWarnings("unchecked")
    private String multipartUpload(
            OSS ossClient,
            MultipartFile file,
            String objectKey,
            String uploadId,
            Consumer<Double> progressCallback) throws IOException {

        long fileSize = file.getSize();
        long partSize = ossConfig.getPartSize();
        int partCount = (int) Math.ceil((double) fileSize / partSize);

        log.info("开始分片上传，总大小: {}, 分片大小: {}, 分片数: {}", fileSize, partSize, partCount);

        InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(
                ossConfig.getBucketName(),
                objectKey
        );

        InitiateMultipartUploadResult initResult = ossClient.initiateMultipartUpload(initRequest);
        String uploadIdOss = initResult.getUploadId();

        try {
            Map<String, String> uploadProgress = new ConcurrentHashMap<>();
            uploadProgress.put("totalSize", String.valueOf(fileSize));
            uploadProgress.put("uploadedSize", "0");
            uploadProgress.put("partCount", String.valueOf(partCount));
            uploadProgress.put("uploadedParts", "0");
            uploadProgress.put("status", "uploading");

            redisUtil.set(REDIS_UPLOAD_PROGRESS_PREFIX + uploadId, uploadProgress);

            PartETag[] partETags = new PartETag[partCount];

            for (int i = 0; i < partCount; i++) {
                long startPos = i * partSize;
                long endPos = Math.min(startPos + partSize, fileSize);
                long partSizeActual = endPos - startPos;

                log.info("上传分片 {}/{}, 大小: {}", i + 1, partCount, partSizeActual);

                UploadPartRequest uploadPartRequest = new UploadPartRequest(
                        ossConfig.getBucketName(),
                        objectKey,
                        uploadIdOss,
                        i + 1,
                        file.getInputStream(),
                        partSizeActual
                );

                UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
                partETags[i] = uploadPartResult.getPartETag();

                long uploadedSize = (i + 1) * partSize;
                if (uploadedSize > fileSize) {
                    uploadedSize = fileSize;
                }

                uploadProgress.put("uploadedSize", String.valueOf(uploadedSize));
                uploadProgress.put("uploadedParts", String.valueOf(i + 1));

                double progress = (double) uploadedSize / fileSize * 100;
                if (progressCallback != null) {
                    progressCallback.accept(progress);
                }

                redisUtil.set(REDIS_UPLOAD_PROGRESS_PREFIX + uploadId, uploadProgress);
            }

            List<PartETag> partETagList = new ArrayList<>();
            for (PartETag partETag : partETags) {
                partETagList.add(partETag);
            }

            CompleteMultipartUploadRequest completeRequest = new CompleteMultipartUploadRequest(
                    ossConfig.getBucketName(),
                    objectKey,
                    uploadIdOss,
                    partETagList
            );

            ossClient.completeMultipartUpload(completeRequest);

            uploadProgress.put("status", "completed");
            uploadProgress.put("progress", "100");
            redisUtil.set(REDIS_UPLOAD_PROGRESS_PREFIX + uploadId, uploadProgress);

            log.info("分片上传完成，文件大小: {}", fileSize);
            return buildFileUrl(objectKey);

        } catch (Exception e) {
            log.error("分片上传失败，尝试取消上传", e);
            try {
                AbortMultipartUploadRequest abortRequest = new AbortMultipartUploadRequest(
                        ossConfig.getBucketName(),
                        objectKey,
                        uploadIdOss
                );
                ossClient.abortMultipartUpload(abortRequest);

                Map<String, String> uploadProgress = new HashMap<>();
                uploadProgress.put("status", "failed");
                uploadProgress.put("error", e.getMessage());
                redisUtil.set(REDIS_UPLOAD_PROGRESS_PREFIX + uploadId, uploadProgress);
            } catch (Exception abortException) {
                log.error("取消上传失败", abortException);
            }
            throw new BizException(ResultCodeEnum.THIRD_PARTY_ERROR, "分片上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            String objectKey = extractObjectKey(fileUrl);
            if (StringUtils.isEmpty(objectKey)) {
                log.warn("无效的文件URL: {}", fileUrl);
                return false;
            }

            OSS ossClient = getOssClient();
            ossClient.deleteObject(ossConfig.getBucketName(), objectKey);
            log.info("文件删除成功: {}", objectKey);
            return true;
        } catch (Exception e) {
            log.error("文件删除失败: {}", fileUrl, e);
            return false;
        }
    }

    @Override
    public String generatePresignedUrl(String fileName, String directory, Long userId, int expireSeconds) {
        try {
            String objectKey = buildObjectKey(directory, fileName);
            OSS ossClient = getOssClient();

            Date expiration = new Date(System.currentTimeMillis() + expireSeconds * 1000L);

            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                    ossConfig.getBucketName(),
                    objectKey
            );
            request.setExpiration(expiration);
            request.setMethod(HttpMethod.PUT);

            URL signedUrl = ossClient.generatePresignedUrl(request);
            log.info("生成预签名URL成功: {}", objectKey);
            return signedUrl.toString();
        } catch (Exception e) {
            log.error("生成预签名URL失败", e);
            throw new BizException(ResultCodeEnum.THIRD_PARTY_ERROR, "生成预签名URL失败: " + e.getMessage(), e);
        }
    }

    @Override
    public UploadProgress checkUploadProgress(String uploadId) {
        try {
            Map<String, String> progressData = (Map<String, String>) redisUtil.get(REDIS_UPLOAD_PROGRESS_PREFIX + uploadId);
            if (progressData == null) {
                return null;
            }

            long totalSize = Long.parseLong(progressData.getOrDefault("totalSize", "0"));
            long uploadedSize = Long.parseLong(progressData.getOrDefault("uploadedSize", "0"));
            int progress = (int) ((double) uploadedSize / totalSize * 100);
            String status = progressData.getOrDefault("status", "unknown");

            return new UploadProgress(uploadId, totalSize, uploadedSize, progress, status);
        } catch (Exception e) {
            log.error("查询上传进度失败", e);
            return null;
        }
    }

    @Override
    public void cancelUpload(String uploadId) {
        try {
            Map<String, String> progressData = (Map<String, String>) redisUtil.get(REDIS_UPLOAD_PROGRESS_PREFIX + uploadId);
            if (progressData == null) {
                log.warn("未找到上传任务: {}", uploadId);
                return;
            }

            String status = progressData.get("status");
            if ("completed".equals(status) || "failed".equals(status)) {
                log.warn("上传任务已完成或失败，无需取消: {}", uploadId);
                return;
            }

            progressData.put("status", "cancelled");
            redisUtil.set(REDIS_UPLOAD_PROGRESS_PREFIX + uploadId, progressData);

            log.info("上传任务已取消: {}", uploadId);
        } catch (Exception e) {
            log.error("取消上传失败", e);
        }
    }

    private static final String[] IMAGE_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "webp", "bmp"};

    @Override
    public List<String> listObjectUrls(String prefix) {
        if (!StringUtils.hasText(prefix)) {
            return new ArrayList<>();
        }
        String normalizedPrefix = prefix.endsWith("/") ? prefix : prefix + "/";
        List<String> urls = new ArrayList<>();
        try {
            OSS ossClient = getOssClient();
            ListObjectsRequest request = new ListObjectsRequest(ossConfig.getBucketName())
                    .withPrefix(normalizedPrefix)
                    .withMaxKeys(100);
            ObjectListing listing = ossClient.listObjects(request);
            for (OSSObjectSummary summary : listing.getObjectSummaries()) {
                String key = summary.getKey();
                if (key.equals(normalizedPrefix)) {
                    continue;
                }
                String ext = getFileExtension(key);
                boolean isImage = false;
                for (String e : IMAGE_EXTENSIONS) {
                    if (e.equalsIgnoreCase(ext)) {
                        isImage = true;
                        break;
                    }
                }
                if (isImage) {
                    urls.add(buildFileUrl(key));
                }
            }
            urls.sort(String::compareTo);
        } catch (Exception e) {
            log.error("列举 OSS 对象失败, prefix={}", normalizedPrefix, e);
        }
        return urls;
    }

    /**
     * 验证文件
     */
    void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        long fileSize = file.getSize();
        if (fileSize > ossConfig.getMaxFileSize()) {
            throw new IllegalArgumentException("文件大小超过限制，最大允许: " +
                    (ossConfig.getMaxFileSize() / 1024 / 1024) + "MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String extension = getFileExtension(originalFilename);
        boolean isAllowed = false;
        for (String allowedExt : ossConfig.getAllowedExtensions()) {
            if (allowedExt.equalsIgnoreCase(extension)) {
                isAllowed = true;
                break;
            }
        }

        if (!isAllowed) {
            throw new IllegalArgumentException("不支持的文件格式: " + extension);
        }

        log.info("文件验证通过，文件名: {}, 大小: {}, 扩展名: {}", originalFilename, fileSize, extension);
    }

    /**
     * 生成文件名（包含时间戳和UUID，避免冲突）
     */
    String generateFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return timestamp + "_" + uuid + "." + extension;
    }

    /**
     * 获取文件扩展名
     */
    String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 构建对象键（OSS存储路径）
     */
    String buildObjectKey(String directory, String fileName) {
        if (StringUtils.isEmpty(directory)) {
            directory = ossConfig.getDirPrefix();
        }
        return directory + "/" + fileName;
    }

    /**
     * 从URL中提取对象键
     */
    String extractObjectKey(String fileUrl) {
        try {
            int bucketIndex = fileUrl.indexOf(ossConfig.getBucketName());
            if (bucketIndex == -1) {
                return null;
            }
            int keyStart = fileUrl.indexOf("/", bucketIndex) + 1;
            return fileUrl.substring(keyStart);
        } catch (Exception e) {
            log.error("提取对象键失败", e);
            return null;
        }
    }

    /**
     * 构建文件访问URL
     */
    String buildFileUrl(String objectKey) {
        return "https://" + ossConfig.getBucketName() + "." +
                ossConfig.getEndpoint().replace("https://", "") +
                "/" + objectKey;
    }

    /**
     * 计算文件MD5
     */
    String calculateMD5(InputStream inputStream) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] buffer = new byte[8192];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            md.update(buffer, 0, bytesRead);
        }

        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }
}
