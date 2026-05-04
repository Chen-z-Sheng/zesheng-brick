package com.zesheng.client.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.function.Consumer;

/**
 * OSS文件上传服务接口
 * 定义文件上传相关的操作
 */
public interface IOssService {

    /**
     * 上传文件（简单上传，适合小文件）
     *
     * @param file     文件对象
     * @param directory 存储目录
     * @param userId    用户ID（用于权限控制）
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, String directory, Long userId);

    /**
     * 上传文件（使用userId作为文件名，用于头像等不暴露敏感信息的场景）
     *
     * @param file     文件对象
     * @param directory 存储目录
     * @param userId   用户ID（用于文件名）
     * @return 文件访问URL
     */
    String uploadFileWithUserId(MultipartFile file, String directory, Long userId);

    /**
     * 分片上传文件（适合大文件，支持断点续传）
     *
     * @param file           文件对象
     * @param directory       存储目录
     * @param userId          用户ID
     * @param uploadId       上传ID（用于断点续传）
     * @param progressCallback 进度回调
     * @return 文件访问URL
     */
    String uploadFileWithMultipart(
            MultipartFile file,
            String directory,
            Long userId,
            String uploadId,
            Consumer<Double> progressCallback);

    /**
     * 删除文件
     *
     * @param fileUrl 文件URL
     * @return 是否删除成功
     */
    boolean deleteFile(String fileUrl);

    /**
     * 生成预签名URL（用于前端直接上传）
     *
     * @param fileName  文件名
     * @param directory 存储目录
     * @param userId    用户ID
     * @param expireSeconds 过期时间（秒）
     * @return 预签名URL
     */
    String generatePresignedUrl(String fileName, String directory, Long userId, int expireSeconds);

    /**
     * 检查上传状态（断点续传）
     *
     * @param uploadId 上传ID
     * @return 上传进度信息
     */
    UploadProgress checkUploadProgress(String uploadId);

    /**
     * 取消上传
     *
     * @param uploadId 上传ID
     */
    void cancelUpload(String uploadId);

    /**
     * 按前缀列出 OSS 对象并返回公网访问 URL 列表（如 index-banner 目录下的图片）
     *
     * @param prefix 对象键前缀，如 "index-banner/"
     * @return 文件公网 URL 列表，按对象键排序
     */
    java.util.List<String> listObjectUrls(String prefix);

    /**
     * 上传进度信息
     */
    class UploadProgress {
        private String uploadId;
        private Long totalSize;
        private Long uploadedSize;
        private Integer progress;
        private String status;

        public UploadProgress(String uploadId, Long totalSize, Long uploadedSize, Integer progress, String status) {
            this.uploadId = uploadId;
            this.totalSize = totalSize;
            this.uploadedSize = uploadedSize;
            this.progress = progress;
            this.status = status;
        }

        public String getUploadId() {
            return uploadId;
        }

        public Long getTotalSize() {
            return totalSize;
        }

        public Long getUploadedSize() {
            return uploadedSize;
        }

        public Integer getProgress() {
            return progress;
        }

        public String getStatus() {
            return status;
        }
    }
}
