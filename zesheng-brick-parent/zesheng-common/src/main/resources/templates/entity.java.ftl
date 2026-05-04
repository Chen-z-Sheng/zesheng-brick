package ${package.Entity};

<#list importEntityFrameworkPackages as pkg>
    import ${pkg};
</#list>

<#list importEntityJavaPackages as pkg>
    import ${pkg};
</#list>

import com.zesheng.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
* ${table.comment!}
* 说明：id/createdAt/updatedAt 已在 BaseEntity 中定义，此处仅生成业务字段
*
* @author ${author}
* @since ${date}
*/
<#list entityClassAnnotations as an>
    ${an.displayName}
</#list>
@Data
@EqualsAndHashCode(callSuper = true) // 继承BaseEntity必须开启
public class ${entity} extends BaseEntity {
<#if entitySerialVersionUID>
    <#if entitySerialAnnotation>
        @Serial
    </#if>
    private static final long serialVersionUID = 1L;
</#if>

<#-- 仅生成业务字段：排除BaseEntity已有的id/created_at/updated_at，以及软删除字段deleted_at -->
<#list table.fields as field>
    <#if field.name != "id" && field.name != "created_at" && field.name != "updated_at" && field.name != "deleted_at">
        <#if field.keyFlag>
            <#assign keyPropertyName="${field.propertyName}"/>
        </#if>

        <#if field.comment!?length gt 0>
            /**
            * ${field.comment}
            */
        </#if>
        <#list field.annotationAttributesList as an>
            ${an.displayName}
        </#list>
        private ${field.propertyType} ${field.propertyName};
    </#if>
</#list>

<#if activeRecord>
    @Override
    public Serializable pkVal() {
    <#if keyPropertyName??>
        return this.${keyPropertyName};
    <#else>
        return this.getId(); // 从BaseEntity获取主键
    </#if>
    }
</#if>
}