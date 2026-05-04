package com.zesheng.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "物流公司新增请求")
public class LogisticsCompanySaveRequest {

    @Schema(description = "公司名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "公司名称不能为空")
    private String name;

    @Schema(description = "公司代码")
    private String code;

    @Schema(description = "排序，升序")
    private Integer sort = 0;

    @Schema(description = "是否启用")
    private Boolean status = Boolean.TRUE;
}
