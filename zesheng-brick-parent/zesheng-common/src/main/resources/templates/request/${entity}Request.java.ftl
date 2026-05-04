package com.zesheng.${moduleName}.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
* ${entity}通用请求参数
*
* @author ${author}
* @since ${date}
*/
@Data
@Schema(name = "${entity}Request", description = "${table.comment}通用请求参数")
public class ${entity}Request {

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
            <#-- 非空校验：String用NotBlank，其他用NotNull -->
            <#if !field.nullable>
                <#if field.propertyType == "String">
                    @NotBlank(message = "${field.comment}不能为空")
                <#else>
                    @NotNull(message = "${field.comment}不能为空")
                </#if>
            </#if>
            private ${field.propertyType} ${field.propertyName};

        </#if>
    </#list>
<#else>
    // 暂无业务字段
</#if>

}
