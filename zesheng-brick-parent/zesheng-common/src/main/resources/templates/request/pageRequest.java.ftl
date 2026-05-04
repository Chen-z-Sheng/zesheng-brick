package com.zesheng.${moduleName}.model.request;

import com.zesheng.common.request.PageAndSortQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
* ${entity}分页查询参数
*
* @author ${author}
* @since ${date}
*/
@Data
@Schema(name = "${entity}PageRequest", description = "${table.comment!?replace('管理端-','')?replace('管理端','')}分页查询参数")
public class ${entity}PageRequest extends PageAndSortQueryRequest {
// 继承PageAndSortQueryRequest：包含pageNum/pageSize/sortField/sortOrder等字段
}