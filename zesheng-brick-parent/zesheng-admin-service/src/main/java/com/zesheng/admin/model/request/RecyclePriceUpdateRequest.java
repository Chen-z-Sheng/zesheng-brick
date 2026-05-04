package com.zesheng.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 行情更新请求
 */
@Data
@Schema(description = "行情更新请求")
public class RecyclePriceUpdateRequest {

    @NotNull(message = "回收价格不能为空")
    @DecimalMin(value = "0", message = "价格不能为负")
    @Schema(description = "回收价格", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal recyclePrice;

    @Schema(description = "备注")
    private String remark;
}
