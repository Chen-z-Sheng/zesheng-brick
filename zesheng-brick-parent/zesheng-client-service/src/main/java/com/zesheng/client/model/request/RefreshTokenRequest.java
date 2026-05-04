package com.zesheng.client.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新令牌请求（与小程序端 JSON body 一致）
 */
@Data
@Schema(description = "刷新令牌请求")
public class RefreshTokenRequest {

    @NotBlank(message = "refreshToken不能为空")
    @Schema(description = "刷新令牌", requiredMode = Schema.RequiredMode.REQUIRED)
    private String refreshToken;
}
