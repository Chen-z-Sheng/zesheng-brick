package com.zesheng.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 三级分类更新请求
 */
@Data
@Schema(description = "三级分类更新请求")
public class CategoryLevel3UpdateRequest {

    @NotBlank(message = "分类名称不能为空")
    @Schema(description = "三级分类名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "状态：1=启用，0=禁用")
    private Integer status;
}
