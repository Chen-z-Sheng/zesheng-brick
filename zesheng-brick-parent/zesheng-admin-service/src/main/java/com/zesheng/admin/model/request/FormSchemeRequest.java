package com.zesheng.admin.model.request;

import com.zesheng.common.enums.StatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(name = "FormSchemeRequest", description = "方案通用请求参数")
public class FormSchemeRequest {

    @Schema(description = "方案名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String name;

    @Schema(description = "关联下单地址ID")
    private Long addressId;

    @Schema(description = "方案说明")
    private String description;

    @Schema(description = "状态：0=停用 1=启用 2=草稿", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"0", "1", "2"}, example = "2")
    @NotNull
    private StatusEnum status = StatusEnum.ENABLE;

    @Schema(description = "每单结算金额，不传时默认为0")
    private BigDecimal unitPrice;
}
