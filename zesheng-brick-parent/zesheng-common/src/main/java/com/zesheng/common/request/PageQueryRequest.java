package com.zesheng.common.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Data
@Schema(description = "基础分页查询参数")
public class PageQueryRequest {

    @Schema(description = "页码（从1开始）", example = "1", defaultValue = "1")
    @Min(value = 1, message = "页码不能小于1")
    @NotNull
    private Integer pageNum = 1; // 自动填充默认值，无需判空

    @Schema(description = "每页条数（1-100）", example = "20", defaultValue = "20")
    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能大于100")
    @NotNull
    private Integer pageSize = 20;
}
