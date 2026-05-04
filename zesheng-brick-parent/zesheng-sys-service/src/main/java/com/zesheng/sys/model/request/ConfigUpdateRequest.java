package com.zesheng.sys.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "系统配置更新请求参数")
public class ConfigUpdateRequest {

    @Schema(description = "配置值原文（文本存储），解析方式由 valueType 决定")
    private String value;

    @Pattern(regexp = "^(json|string|number|boolean)$", message = "值类型只能是 json/string/number/boolean")
    @Schema(description = "值类型")
    private String valueType;

    @Schema(description = "备注")
    private String remark;
}
