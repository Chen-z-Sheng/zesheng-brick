package com.zesheng.${moduleName}.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
* ${entity}更新响应
*
* @author ${author}
* @since ${date}
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "${entity}UpdateResponse", description = "${table.comment!?replace('管理端-','')?replace('管理端','')}更新响应")
public class ${entity}UpdateResponse extends ${entity}Vo {
// ID已在Vo中声明，此处无需重复定义（若需显式可保留）
@Schema(description = "${table.comment!?replace('管理端-','')?replace('管理端','')}主键ID", example = "6")
private Long id;
}