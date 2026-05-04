package com.zesheng.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "下单地址新增请求")
public class DeliveryAddressSaveRequest {

    @Schema(description = "地址名称/备注", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "地址名称不能为空")
    private String name;

    @Schema(description = "完整地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "完整地址不能为空")
    private String fullAddress;

    @Schema(description = "排序号，升序")
    private Integer sortOrder = 0;

    @Schema(description = "状态：0=禁用 1=启用")
    private Integer status = 1;
}
