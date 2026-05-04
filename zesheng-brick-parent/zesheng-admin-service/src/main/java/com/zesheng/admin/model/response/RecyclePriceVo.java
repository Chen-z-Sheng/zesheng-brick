package com.zesheng.admin.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 行情列表视图对象
 */
@Data
@Schema(description = "行情记录VO")
public class RecyclePriceVo {

    @Schema(description = "行情ID")
    private Long id;

    @Schema(description = "二级分类ID")
    private Long level2Id;

    @Schema(description = "三级分类ID")
    private Long level3Id;

    @Schema(description = "一级分类ID")
    private Long level1Id;

    @Schema(description = "一级分类名称")
    private String level1Name;

    @Schema(description = "二级分类名称")
    private String level2Name;

    @Schema(description = "三级分类名称")
    private String level3Name;

    @Schema(description = "行情日期")
    private LocalDate priceDate;

    @Schema(description = "回收价格")
    private BigDecimal recyclePrice;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
