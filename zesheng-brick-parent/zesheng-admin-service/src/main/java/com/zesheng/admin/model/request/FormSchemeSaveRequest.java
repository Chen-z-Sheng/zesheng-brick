package com.zesheng.admin.model.request;

import com.zesheng.common.entity.BaseEntity;
import com.zesheng.common.enums.StatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(name = "FormSchemeSaveRequest", description = "方案新增请求参数")
public class FormSchemeSaveRequest extends FormSchemeRequest{
}
