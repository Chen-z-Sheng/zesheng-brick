package com.zesheng.client.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新用户信息请求
 */
@Data
@Schema(description = "更新用户信息请求")
public class UpdateUserInfoRequest {

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    private String nickName;

    /**
     * 头像URL
     */
    @Schema(description = "头像URL")
    private String avatarUrl;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String phone;
}
