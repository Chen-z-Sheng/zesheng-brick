package com.zesheng.client.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OSS上传响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OSS上传响应")
public class OssUploadResponse {

    /**
     * 文件访问URL
     */
    @Schema(description = "文件访问URL")
    private String fileUrl;

    /**
     * 上传ID
     */
    @Schema(description = "上传ID")
    private String uploadId;

    /**
     * 文件大小
     */
    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    /**
     * 文件名
     */
    @Schema(description = "文件名")
    private String fileName;
}
