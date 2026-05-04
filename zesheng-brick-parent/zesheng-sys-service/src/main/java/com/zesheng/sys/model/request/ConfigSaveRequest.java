package com.zesheng.sys.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "系统配置新增请求参数")
public class ConfigSaveRequest {

    @NotBlank(message = "配置键不能为空")
    @Schema(description = "配置键，建议用点号分层如 site.name")
    private String configKey;

    @NotBlank(message = "配置值不能为空")
    @Schema(description = "配置值原文（文本存储），解析方式由 valueType 决定")
    private String value;

    @Pattern(regexp = "^(json|string|number|boolean)$", message = "值类型只能是 json/string/number/boolean")
    @Schema(description = "值类型", example = "json")
    private String valueType = "json";

    @Schema(description = "备注")
    private String remark;
}
