package com.zesheng.client.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 绑定手机号请求
 */
@Data
@Schema(description = "绑定手机号请求")
public class BindPhoneRequest {

    /**
     * 微信手机号获取凭证code
     */
    @NotBlank(message = "code不能为空")
    @Schema(description = "微信手机号获取凭证code", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    /**
     * 微信openid
     */
    @NotBlank(message = "openid不能为空")
    @Schema(description = "微信openid", requiredMode = Schema.RequiredMode.REQUIRED)
    private String openid;
}
