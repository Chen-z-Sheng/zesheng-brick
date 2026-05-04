package com.zesheng.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "物流公司修改请求")
public class LogisticsCompanyUpdateRequest {

    @Schema(description = "公司名称")
    private String name;

    @Schema(description = "公司代码")
    private String code;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "是否启用")
    private Boolean status;
}
