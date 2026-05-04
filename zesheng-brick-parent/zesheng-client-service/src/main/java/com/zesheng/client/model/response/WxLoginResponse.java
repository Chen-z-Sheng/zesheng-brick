package com.zesheng.client.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 微信登录响应（与前端 userInfo 字段一致：userId, nickName, avatarUrl, phone）
 */
@Data
@Schema(description = "微信登录响应")
public class WxLoginResponse {

    @Schema(description = "JWT访问令牌")
    private String token;

    @Schema(description = "JWT刷新令牌")
    private String refreshToken;

    @Schema(description = "用户信息，前端存 storage 与页面 data.userInfo 使用同名字段")
    private UserInfo userInfo;

    @Schema(description = "用户是否已注册")
    private boolean registered = true;

    @Schema(description = "是否是新注册用户")
    private boolean isNewUser = false;

    /**
     * 用户信息，与 client_user 表部分字段对应，供前端展示与更新
     */
    @Data
    @Schema(description = "用户信息")
    public static class UserInfo {

        @Schema(description = "用户ID")
        private Long userId;

        @Schema(description = "用户昵称")
        private String nickName;

        @Schema(description = "头像URL，前端展示可用 avatarUrl 或 avatar 兜底")
        private String avatarUrl;

        @Schema(description = "手机号")
        private String phone;
    }
}
