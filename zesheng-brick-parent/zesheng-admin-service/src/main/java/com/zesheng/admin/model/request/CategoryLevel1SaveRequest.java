package com.zesheng.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 一级分类新增请求
 */
@Data
@Schema(description = "一级分类新增请求")
public class CategoryLevel1SaveRequest {

    @NotBlank(message = "分类名称不能为空")
    @Schema(description = "大分类名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "排序号", defaultValue = "0")
    private Integer sortOrder = 0;

    @Schema(description = "状态：1=启用，0=禁用", defaultValue = "1")
    private Integer status = 1;
}
