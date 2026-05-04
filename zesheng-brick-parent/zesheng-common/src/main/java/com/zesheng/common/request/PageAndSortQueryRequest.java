package com.zesheng.common.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.Pattern;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "带排序的分页查询参数")
public class PageAndSortQueryRequest extends PageQueryRequest {

    @Schema(description = "排序字段", example = "updatedAt")
    private String orderBy;

    @Schema(description = "排序方向", example = "DESC", defaultValue = "DESC")
    @Pattern(regexp = "^(ASC|DESC)$", message = "排序方向只能是ASC或DESC")
    private String order = "DESC";
}
