package ${package.Mapper};

<#list importMapperFrameworkPackages as pkg>
    import ${pkg};
</#list>
<#if importMapperJavaPackages?size !=0>
    <#list importMapperJavaPackages as pkg>
        import ${pkg};
    </#list>
</#if>

import org.springframework.stereotype.Repository; // 标识Mapper组件，消除IDEA提示

/**
* ${table.comment!} Mapper 接口
*
* @author ${author}
* @since ${date}
*/
<#if mapperAnnotationClass??>
    @${mapperAnnotationClass.simpleName}
</#if>
@Repository // 生产级规范：标注Mapper为Spring组件
public interface ${table.mapperName} extends ${superMapperClass}<${entity}> {

}