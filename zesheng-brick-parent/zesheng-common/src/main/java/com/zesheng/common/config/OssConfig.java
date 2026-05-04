package com.zesheng.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云OSS配置类
 * 从环境变量或配置文件中读取OSS相关配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssConfig {

    /**
     * OSS访问域名
     */
    private String endpoint;

    /**
     * OSS访问密钥ID
     */
    private String accessKeyId;

    /**
     * OSS访问密钥Secret
     */
    private String accessKeySecret;

    /**
     * OSS存储空间名称
     */
    private String bucketName;

    /**
     * OSS文件存储目录前缀
     */
    private String dirPrefix = "uploads";

    /**
     * 文件大小限制（字节），默认10MB
     */
    private Long maxFileSize = 10 * 1024 * 1024L;

    /**
     * 允许的文件扩展名
     */
    private String[] allowedExtensions = {"jpg", "jpeg", "png", "webp", "gif"};

    /**
     * 分片上传时的分片大小（字节），默认1MB
     */
    private Long partSize = 1024 * 1024L;

    /**
     * 分片上传的并发数
     */
    private Integer uploadConcurrency = 3;
}
