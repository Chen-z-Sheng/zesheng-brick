package com.zesheng.admin.model.response;

import com.zesheng.common.response.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "FormSchemePageResponse", description = "方案分页查询响应参数")
public class FormSchemePageResponse extends PageResult<FormSchemeVo> {
}
