package com.zesheng.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 当前登录用户修改登录密码
 */
@Data
@Schema(name = "PasswordChangeRequest", description = "修改密码请求")
public class PasswordChangeRequest {

    @Schema(description = "当前密码")
    @NotBlank(message = "请输入当前密码")
    private String oldPassword;

    @Schema(description = "新密码")
    @NotBlank(message = "请输入新密码")
    @Size(min = 6, max = 64, message = "新密码长度为6~64个字符")
    private String newPassword;
}
