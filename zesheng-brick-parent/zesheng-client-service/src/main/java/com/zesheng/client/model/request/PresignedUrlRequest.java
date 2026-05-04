package com.zesheng.client.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 预签名URL请求
 */
@Data
@Schema(description = "预签名URL请求")
public class PresignedUrlRequest {

    /**
     * 文件名
     */
    @NotBlank(message = "文件名不能为空")
    @Schema(description = "文件名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileName;

    /**
     * 存储目录
     */
    @Schema(description = "存储目录")
    private String directory;

    /**
     * 过期时间（秒）
     */
    @NotNull(message = "过期时间不能为空")
    @Schema(description = "过期时间（秒）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer expireSeconds;
}
