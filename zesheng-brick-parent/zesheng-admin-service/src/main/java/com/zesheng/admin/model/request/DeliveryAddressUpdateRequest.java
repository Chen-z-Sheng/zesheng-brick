package com.zesheng.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "下单地址修改请求")
public class DeliveryAddressUpdateRequest {

    @Schema(description = "地址名称/备注")
    private String name;

    @Schema(description = "完整地址")
    private String fullAddress;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "状态：0=禁用 1=启用")
    @Min(value = 0, message = "状态取值非法")
    @Max(value = 1, message = "状态取值非法")
    private Integer status;
}
