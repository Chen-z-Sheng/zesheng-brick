package com.zesheng.${moduleName}.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
* ${entity}详情响应
* 说明：包含所有字段（业务字段+BaseEntity字段）
*
* @author ${author}
* @since ${date}
*/
@Data
@Schema(name = "${entity}Vo", description = "${table.comment!?replace('管理端-','')?replace('管理端','')}详情响应")
public class ${entity}Vo {
// BaseEntity字段（显式声明，便于Swagger文档展示）
@Schema(description = "主键ID", example = "1")
private Long id;

@Schema(description = "创建时间", example = "2026-02-18T16:00:00")
private LocalDateTime createdAt;

@Schema(description = "更新时间", example = "2026-02-18T16:00:00")
private LocalDateTime updatedAt;

// 业务字段
<#if table.fields??>
<#list table.fields as field>
<#if field.name != "id" && field.name != "created_at" && field.name != "updated_at" && field.name != "deleted_at">
<#-- 预定义示例值，避免在插值中写复杂表达式 -->
<#assign exampleValue = "true">
<#if field.propertyType == 'String'>
<#assign exampleValue = "示例值">
<#elseif field.propertyType == 'Long'>
<#assign exampleValue = "1">
</#if>

    @Schema(description = "${field.comment}", example = "${exampleValue}")
    private ${field.propertyType} ${field.propertyName};

</#if>
</#list>
<#else>
    // 暂无业务字段
</#if>
}