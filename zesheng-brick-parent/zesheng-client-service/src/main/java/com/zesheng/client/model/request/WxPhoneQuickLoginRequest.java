package com.zesheng.client.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "手机号快捷登录")
public class WxPhoneQuickLoginRequest {

    @NotBlank(message = "loginCode不能为空")
    @Schema(description = "wx.login 临时凭证")
    private String loginCode;

    @NotBlank(message = "phoneCode不能为空")
    @Schema(description = "getPhoneNumber 动态令牌")
    private String phoneCode;
}
