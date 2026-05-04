package com.zesheng.${moduleName}.model.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
* ${entity}列表响应
* 说明：继承Vo，复用所有字段
*
* @author ${author}
* @since ${date}
*/
@Schema(name = "${entity}ListResponse", description = "${table.comment!?replace('管理端-','')?replace('管理端','')}列表响应")
public class ${entity}ListResponse extends ${entity}Vo {
// 仅继承Vo，无额外字段
}