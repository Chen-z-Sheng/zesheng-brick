<#assign BUSINESS_NAME = table.comment!?replace('管理端-','')?replace('管理端','')>
package ${package.ServiceImpl};

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zesheng.${package.ModuleName}.entity.${entity};
import com.zesheng.${package.ModuleName}.mapper.${table.mapperName};
import com.zesheng.${package.ModuleName}.model.request.${entity}PageRequest;
import com.zesheng.${package.ModuleName}.model.request.${entity}SaveRequest;
import com.zesheng.${package.ModuleName}.model.request.${entity}UpdateRequest;
import com.zesheng.${package.ModuleName}.model.response.${entity}ListResponse;
import com.zesheng.${package.ModuleName}.model.response.${entity}PageResponse;
import com.zesheng.${package.ModuleName}.model.response.${entity}SaveResponse;
import com.zesheng.${package.ModuleName}.model.response.${entity}UpdateResponse;
import com.zesheng.${package.ModuleName}.model.response.${entity}Vo;
import com.zesheng.${package.ModuleName}.service.${table.serviceName};
import com.zesheng.common.response.R;
import com.zesheng.common.response.PageMeta;
import com.zesheng.common.util.BeanCopyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.zesheng.common.response.BatchDeleteResponse;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* ${table.comment!?replace('管理端-','')?replace('管理端','')} 服务实现类
*
* @author ${author}
* @since ${date}
*/
@Service
@RequiredArgsConstructor
@Slf4j // 生产级日志
public class ${table.serviceImplName} implements ${table.serviceName} {
private final ${table.mapperName} ${table.entityPath}Mapper;

/**
* 新增${BUSINESS_NAME}
* 核心：新增后查询数据库，补全BaseEntity的createdAt/updatedAt
*/
@Override
public R${'<'}${entity}SaveResponse${'>'} save(${entity}SaveRequest saveRequest) {
// DTO转实体（仅复制业务字段，BaseEntity字段由MyBatis-Plus自动填充）
${entity} entity = BeanCopyUtils.copyIgnoreNull(saveRequest, ${entity}.class);

// 执行新增
int insertCount = ${table.entityPath}Mapper.insert(entity);
if (insertCount <= 0) {
log.error("【新增{}】失败，请求参数：{}", BUSINESS_NAME, saveRequest);
return R.error("新增" + BUSINESS_NAME + "失败");
}

// 关键：查询数据库获取完整实体（含createdAt/updatedAt）
${entity} savedEntity = ${table.entityPath}Mapper.selectById(entity.getId());
Assert.notNull(savedEntity, "新增" + BUSINESS_NAME + "成功，但查询不到数据，ID：" + entity.getId());

// 实体转返回体（自动包含createdAt/updatedAt）
${entity}SaveResponse response = BeanCopyUtils.copyIgnoreNull(savedEntity, ${entity}SaveResponse.class);
response.setId(savedEntity.getId()); // 显式赋值ID，确保非空

log.info("【新增{}】成功，ID：{}", BUSINESS_NAME, savedEntity.getId());
return R.success(response);
}

/**
* 单个删除${BUSINESS_NAME}
* 自动适配：有deleted_at字段用软删除，无则物理删除
*/
@Override
public R<Void> delete(Long id) {
    Assert.notNull(id, BUSINESS_NAME + "ID不能为空");

    <#-- 判断表是否有软删除字段 -->
    <#assign hasDeletedAt = false>
    <#list table.fields as field>
        <#if field.name == "deleted_at">
            <#assign hasDeletedAt = true>
            <#break>
        </#if>
    </#list>

    // 查询数据是否存在
    LambdaQueryWrapper<${entity}> queryWrapper = new LambdaQueryWrapper<${entity}>()
            .eq(${entity}::getId, id);
    <#if hasDeletedAt>
    queryWrapper.isNull(${entity}::getDeletedAt);
    </#if>

    ${entity} existEntity = ${table.entityPath}Mapper.selectOne(queryWrapper);
    if (existEntity == null) {
        log.error("【删除{}】失败，ID：{}不存在", BUSINESS_NAME, id);
        return R.error(BUSINESS_NAME + "ID不存在");
    }

    int deleteCount;
    <#if hasDeletedAt>
    // 执行软删除
    deleteCount = ${table.entityPath}Mapper.update(null, new LambdaUpdateWrapper<${entity}>()
            .eq(${entity}::getId, id)
            .set(${entity}::getDeletedAt, LocalDateTime.now()));
    <#else>
    // 执行物理删除
    deleteCount = ${table.entityPath}Mapper.deleteById(id);
    </#if>

    if (deleteCount <= 0) {
        log.error("【删除{}】失败，ID：{}", BUSINESS_NAME, id);
        return R.error("删除" + BUSINESS_NAME + "失败");
    }

    log.info("【删除{}】成功，ID：{}", BUSINESS_NAME, id);
    return R.success();
}

/**
* 批量删除${BUSINESS_NAME}
* 自动适配：有deleted_at字段用软删除，无则物理删除
*/
@Override
public R<BatchDeleteResponse> delete(List<Long> ids) {
    if (CollectionUtils.isEmpty(ids)) {
        return R.error("删除ID列表不能为空");
    }

    <#-- 判断表是否有软删除字段 -->
    <#assign hasDeletedAt = false>
    <#list table.fields as field>
        <#if field.name == "deleted_at">
            <#assign hasDeletedAt = true>
            <#break>
        </#if>
    </#list>

    BatchDeleteResponse response = new BatchDeleteResponse();
    response.setTotalRequest(ids.size());

    // 查询实际存在的ID列表
    LambdaQueryWrapper<${entity}> queryWrapper = new LambdaQueryWrapper<${entity}>()
            .in(${entity}::getId, ids);
    <#if hasDeletedAt>
    queryWrapper.isNull(${entity}::getDeletedAt);
    </#if>

    List<Long> existIds = ${table.entityPath}Mapper.selectObjs(queryWrapper)
            .stream()
            .map(obj -> Long.parseLong(obj.toString()))
            .collect(Collectors.toList());

    // 处理不存在的ID
    Map<Serializable, String> failedIds = new HashMap<>();
    for (Long id : ids) {
        if (!existIds.contains(id)) {
            failedIds.put(id, "数据不存在");
        }
    }
    response.setFailedIds(failedIds);

    if (!CollectionUtils.isEmpty(existIds)) {
        int deleteCount;
        <#if hasDeletedAt>
        // 执行软删除
        deleteCount = ${table.entityPath}Mapper.update(null, new LambdaUpdateWrapper<${entity}>()
                .in(${entity}::getId, existIds)
                .set(${entity}::getDeletedAt, LocalDateTime.now()));
        <#else>
        // 执行物理删除
        deleteCount = ${table.entityPath}Mapper.delete(new LambdaQueryWrapper<${entity}>()
                .in(${entity}::getId, existIds));
        </#if>

        if (deleteCount > 0) {
            response.setSuccessIds(existIds.stream()
                    .map(id -> (Serializable) id)
                    .collect(Collectors.toList()));
        }
    }

    response.calculateCount();
    log.info("【批量删除{}】总请求数：{}，成功删除：{}，失败：{}", 
            BUSINESS_NAME, response.getTotalRequest(), 
            response.getSuccessCount(), response.getFailedCount());
    
    String msg = "批量删除完成，成功" + response.getSuccessCount() + "条，失败" + response.getFailedCount() + "条";
    return R.success(msg, response);
}

/**
* 修改${BUSINESS_NAME}
* 核心：更新后查询数据库，补全updatedAt
*/
@Override
public R${'<'}${entity}UpdateResponse${'>'} update(Long id, ${entity}UpdateRequest updateRequest) {
// 参数校验
Assert.notNull(id, BUSINESS_NAME + "ID不能为空");
Assert.notNull(updateRequest, "更新请求参数不能为空");

// 检查数据是否存在
${entity} existEntity = ${table.entityPath}Mapper.selectById(id);
if (existEntity == null) {
log.error("【修改{}】失败，ID：{}不存在", BUSINESS_NAME, id);
return R.error("要修改的" + BUSINESS_NAME + "不存在");
}

// 复制更新参数到已有实体（必须用 copyIgnoreNullToExist，勿用 copyIgnoreNull(updateRequest, existEntity.getClass())）
BeanCopyUtils.copyIgnoreNullToExist(updateRequest, existEntity);

// 执行更新
int updateCount = ${table.entityPath}Mapper.updateById(existEntity);
if (updateCount <= 0) {
log.error("【修改{}】失败，ID：{}，请求参数：{}", BUSINESS_NAME, id, updateRequest);
return R.error("修改" + BUSINESS_NAME + "失败");
}

// 查询最新数据（含更新后的updatedAt）
${entity} updatedEntity = ${table.entityPath}Mapper.selectById(id);

// 转换为返回体
${entity}UpdateResponse response = BeanCopyUtils.copyIgnoreNull(updatedEntity, ${entity}UpdateResponse.class);
response.setId(id);

log.info("【修改{}】成功，ID：{}", BUSINESS_NAME, id);
return R.success(response);
}

/**
* 分页查询${BUSINESS_NAME}
* 优化：校验分页参数，排序用BaseEntity的createdAt
*/
@Override
public R${'<'}${entity}PageResponse${'>'} page(${entity}PageRequest pageRequest) {
// 分页参数校验（避免无效参数）
int pageNum = pageRequest.getPageNum() <= 0 ? 1 : pageRequest.getPageNum();
int pageSize = pageRequest.getPageSize() <= 0 ? 10 : pageRequest.getPageSize();
pageSize = pageSize > 100 ? 100 : pageSize; // 限制最大页大小

// 构建分页查询
Page<${entity}> page = new Page<>(pageNum, pageSize);
LambdaQueryWrapper<${entity}> queryWrapper = new LambdaQueryWrapper<${entity}>()
.orderByDesc(${entity}::getCreatedAt); // 匹配BaseEntity的createdAt

IPage<${entity}> iPage = ${table.entityPath}Mapper.selectPage(page, queryWrapper);

// 转换为VO分页对象（统一使用BeanCopyUtils）
IPage${'<'}${entity}Vo${'>'} voPage = iPage.convert(entity -> BeanCopyUtils.copyIgnoreNull(entity, ${entity}Vo.class));

// 构建分页返回体
${entity}PageResponse response = new ${entity}PageResponse();
PageMeta pageMeta = PageMeta.of(voPage.getTotal(), (int) voPage.getCurrent(), (int) voPage.getSize());
response.setPageMeta(pageMeta);
response.setRecords(voPage.getRecords());

log.info("【分页查询{}】页码：{}，页大小：{}，总数：{}", BUSINESS_NAME, pageNum, pageSize, voPage.getTotal());
return R.success(response);
}

/**
* 查询${BUSINESS_NAME}列表（不分页）
*/
@Override
public R${'<'}List${'<'}${entity}ListResponse${'>'}${'>'} list() {
List<${entity}> entityList = ${table.entityPath}Mapper.selectList(new LambdaQueryWrapper<${entity}>()
.orderByDesc(${entity}::getCreatedAt));

// 转换为ListResponse（继承Vo，无需重复赋值）
List${'<'}${entity}ListResponse${'>'} responseList = entityList.stream()
.map(entity -> BeanCopyUtils.copyIgnoreNull(entity, ${entity}ListResponse.class))
.collect(Collectors.toList());

log.info("【查询{}列表】总数：{}", BUSINESS_NAME, responseList.size());
return R.success(responseList);
}

/**
* 查询${BUSINESS_NAME}详情
*/
@Override
public R${'<'}${entity}Vo${'>'} info(Long id) {
Assert.notNull(id, BUSINESS_NAME + "ID不能为空");

${entity} entity = ${table.entityPath}Mapper.selectById(id);
if (entity == null) {
log.error("【查询{}详情】ID：{}不存在", BUSINESS_NAME, id);
return R.error(BUSINESS_NAME + "不存在");
}

${entity}Vo vo = BeanCopyUtils.copyIgnoreNull(entity, ${entity}Vo.class);
return R.success(vo);
}
}