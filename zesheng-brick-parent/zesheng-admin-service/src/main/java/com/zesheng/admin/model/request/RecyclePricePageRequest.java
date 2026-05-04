package com.zesheng.admin.model.request;

import com.zesheng.common.request.PageAndSortQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 行情分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "行情分页查询参数")
public class RecyclePricePageRequest extends PageAndSortQueryRequest {

    @Schema(description = "一级分类ID")
    private Long level1Id;

    @Schema(description = "二级分类ID")
    private Long level2Id;

    @Schema(description = "三级分类ID")
    private Long level3Id;

    @Schema(description = "行情日期")
    private LocalDate priceDate;
}
