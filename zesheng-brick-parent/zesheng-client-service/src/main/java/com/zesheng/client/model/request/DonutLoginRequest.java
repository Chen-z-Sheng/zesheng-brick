package com.zesheng.client.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "多端身份登录")
public class DonutLoginRequest {

    @NotBlank(message = "code不能为空")
    @Schema(description = "微信返回的临时登录 code")
    private String code;
}
