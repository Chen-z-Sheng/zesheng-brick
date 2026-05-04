package com.zesheng.${moduleName}.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
* ${entity}更新请求参数
* 说明：更新不做非空校验（支持部分字段修改）
*
* @author ${author}
* @since ${date}
*/
@Data
@Schema(name = "${entity}UpdateRequest", description = "${table.comment!?replace('管理端-','')?replace('管理端','')}更新请求参数")
public class ${entity}UpdateRequest {

<#if table.fields??>
    <#list table.fields as field>
    <#-- 排除BaseEntity字段和软删除字段 -->
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