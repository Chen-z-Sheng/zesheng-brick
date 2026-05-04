package com.zesheng.admin.model.request;

import com.zesheng.common.request.PageAndSortQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "FormSchemePageRequest", description = "方案分页查询参数")
public class FormSchemePageRequest extends PageAndSortQueryRequest {

    @Schema(description = "方案名称关键字（模糊匹配）")
    private String name;

    @Schema(description = "状态：0停用 1启用 2草稿")
    private Integer status;
}
