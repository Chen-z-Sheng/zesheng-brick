package com.zesheng.client.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信登录请求
 */
@Data
@Schema(description = "微信登录请求")
public class WxLoginRequest {

    /**
     * 微信登录凭证code
     */
    @NotBlank(message = "code不能为空")
    @Schema(description = "微信登录凭证code", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;
}
