package com.zesheng.client.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * OSS上传请求
 */
@Data
@Schema(description = "OSS上传请求")
public class OssUploadRequest {

    /**
     * 存储目录
     */
    @Schema(description = "存储目录")
    private String directory;

    /**
     * 上传ID（用于断点续传）
     */
    @Schema(description = "上传ID（用于断点续传）")
    private String uploadId;
}
