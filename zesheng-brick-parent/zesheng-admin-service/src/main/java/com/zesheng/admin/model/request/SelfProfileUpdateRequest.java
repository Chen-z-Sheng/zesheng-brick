package com.zesheng.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 当前登录用户自助修改的资料字段（不含用户名、角色等敏感项）
 */
@Data
@Schema(name = "SelfProfileUpdateRequest", description = "当前用户资料更新请求")
public class SelfProfileUpdateRequest {

    @Schema(description = "手机号")
    @Size(max = 32, message = "手机号长度不能超过32个字符")
    private String phone;

    @Schema(description = "头像链接")
    @Size(max = 512, message = "头像链接长度不能超过512个字符")
    private String avatarUrl;
}
