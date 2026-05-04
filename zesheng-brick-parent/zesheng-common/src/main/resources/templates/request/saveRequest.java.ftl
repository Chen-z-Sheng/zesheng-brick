package com.zesheng.${moduleName}.model.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
* ${entity}新增请求参数
* 说明：继承通用Request，复用非空校验规则
*
* @author ${author}
* @since ${date}
*/
@Schema(name = "${entity}SaveRequest", description = "${table.comment!?replace('管理端-','')?replace('管理端','')}新增请求参数")
public class ${entity}SaveRequest extends ${entity}Request {
// 仅继承通用Request，无额外字段
}