package com.zesheng.admin.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "LoginResponse", description = "登录请求返回参数")
public class LoginResponse {
    @Schema(description = "访问令牌")
    private String accessToken;

    @Schema(description = "令牌类型")
    private String tokenType;

    @Schema(description = "令牌有效期（秒）")
    private Integer expiresIn;

    @Schema(description = "刷新令牌（用于访问令牌过期后免登录刷新）")
    private String refreshToken;
}
