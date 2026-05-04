package com.zesheng.admin.model.request;

import com.zesheng.common.enums.StatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(name = "FormSchemeUpdateRequest", description = "方案修改请求参数")
public class FormSchemeUpdateRequest {

    @Schema(description = "方案名称", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "xxx方案")
    private String name;

    @Schema(description = "关联下单地址ID")
    private Long addressId;

    @Schema(description = "方案说明", example = "老板结账慢")
    private String description;

    @Schema(description = "状态：0=停用 1=启用 2=草稿", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "0")
    private StatusEnum status;

    @Schema(description = "每单结算金额", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "66")
    private BigDecimal unitPrice;
}
