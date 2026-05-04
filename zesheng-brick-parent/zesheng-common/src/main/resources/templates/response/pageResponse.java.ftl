package com.zesheng.${moduleName}.model.response;

import com.zesheng.common.response.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;

/**
* ${entity}分页响应
*
* @author ${author}
* @since ${date}
*/
@Schema(name = "${entity}PageResponse", description = "${table.comment!?replace('管理端-','')?replace('管理端','')}分页响应")
public class ${entity}PageResponse extends PageResult${'<'}${entity}Vo${'>'} {
// 继承PageResult：包含pageMeta（分页元信息）和records（数据列表）
}