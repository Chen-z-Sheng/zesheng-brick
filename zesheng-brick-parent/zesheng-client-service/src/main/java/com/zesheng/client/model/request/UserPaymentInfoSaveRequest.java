package com.zesheng.client.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "用户收款信息新增请求")
public class UserPaymentInfoSaveRequest {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    @NotBlank(message = "真实姓名不能为空")
    @Schema(description = "真实姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String realName;

    @Schema(description = "支付宝账号")
    private String alipayAccount;

    @Schema(description = "微信收款码图片URL")
    private String wechatQrcode;

    @Schema(description = "支付宝收款码图片URL")
    private String alipayQrcode;

    @Schema(description = "银行卡号")
    private String bankCardNo;

    @Schema(description = "开户银行")
    private String bankName;

    @Schema(description = "开户支行")
    private String bankBranch;
}
