package com.zesheng.admin.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "FormSchemeUpdateResponse", description = "方案修改响应参数")
public class FormSchemeUpdateResponse extends FormSchemeVo {
    @Schema(description = "方案主键ID", example = "6")
    private Long id;
}
