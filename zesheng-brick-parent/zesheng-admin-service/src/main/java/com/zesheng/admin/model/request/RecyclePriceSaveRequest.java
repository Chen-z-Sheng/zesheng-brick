package com.zesheng.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 行情新增请求
 */
@Data
@Schema(description = "行情新增请求")
public class RecyclePriceSaveRequest {

    @NotNull(message = "三级分类ID不能为空")
    @Schema(description = "三级分类ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long level3Id;

    @NotNull(message = "行情日期不能为空")
    @Schema(description = "行情日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate priceDate;

    @NotNull(message = "回收价格不能为空")
    @DecimalMin(value = "0", message = "价格不能为负")
    @Schema(description = "回收价格", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal recyclePrice;

    @Schema(description = "备注")
    private String remark;
}
