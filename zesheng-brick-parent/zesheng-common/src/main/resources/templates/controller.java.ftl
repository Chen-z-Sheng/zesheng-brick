<#assign BUSINESS_NAME = table.comment!?replace('管理端-','')?replace('管理端','')>
package ${package.Controller};

import com.zesheng.${package.ModuleName}.model.request.${entity}PageRequest;
import com.zesheng.${package.ModuleName}.model.request.${entity}SaveRequest;
import com.zesheng.${package.ModuleName}.model.request.${entity}UpdateRequest;
import com.zesheng.${package.ModuleName}.model.response.${entity}ListResponse;
import com.zesheng.${package.ModuleName}.model.response.${entity}PageResponse;
import com.zesheng.${package.ModuleName}.model.response.${entity}SaveResponse;
import com.zesheng.${package.ModuleName}.model.response.${entity}UpdateResponse;
import com.zesheng.${package.ModuleName}.model.response.${entity}Vo;
import com.zesheng.${package.ModuleName}.service.I${entity}Service;
import com.zesheng.common.response.BatchDeleteResponse;
import com.zesheng.common.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
* ${table.comment!}控制器
*
* @author ${author}
* @since ${date}
*/
@RestController
@RequestMapping("${table.entityPath}")
@Tag(name = "${table.comment!}模块", description = "${table.comment!}相关接口")
@RequiredArgsConstructor
@Slf4j // 生产级日志：便于排查问题
public class ${table.controllerName} {
private final I${entity}Service ${table.entityPath}Service;
// 抽离常量：避免重复替换注释
private static final String BUSINESS_NAME = "${table.comment!?replace('管理端-','')?replace('管理端','')}";

/**
* 新增${BUSINESS_NAME}
*
* @param saveRequest 新增请求参数（仅含业务字段）
* @return 新增结果（含ID、数据库自动填充的创建/更新时间）
*/
@PostMapping
@Operation(summary = "新增${BUSINESS_NAME}")
public R${'<'}${entity}SaveResponse${'>'} add(@Validated @RequestBody ${entity}SaveRequest saveRequest) {
log.info("【新增{}】请求参数：{}", BUSINESS_NAME, saveRequest);
return ${table.entityPath}Service.save(saveRequest);
}

/**
* 单个删除${BUSINESS_NAME}
*
* @param id 要删除的ID
* @return 删除结果
*/
@DeleteMapping("/delete/{id}")
@Operation(summary = "单个删除${BUSINESS_NAME}")
public R<Void> delete(@Parameter(description = "要删除的ID") @PathVariable Long id) {
    log.info("【删除{}】ID：{}", BUSINESS_NAME, id);
    return ${table.entityPath}Service.delete(id);
}

/**
* 批量删除${BUSINESS_NAME}
*
* @param ids 要删除的ID列表
* @return 批量删除详细结果
*/
@PostMapping("/delete/batch")
@Operation(summary = "批量删除${BUSINESS_NAME}")
public R<BatchDeleteResponse> deleteBatch(@RequestBody List<Long> ids) {
    log.info("【批量删除{}】ID列表：{}", BUSINESS_NAME, ids);
    return ${table.entityPath}Service.delete(ids);
}

/**
* 修改${BUSINESS_NAME}
*
* @param id 要修改的${BUSINESS_NAME}ID
* @param updateRequest 更新请求参数（仅含业务字段）
* @return 更新结果（含最新的更新时间）
*/
@PatchMapping("/{id}")
@Operation(summary = "修改${BUSINESS_NAME}")
public R${'<'}${entity}UpdateResponse${'>'} update(
@PathVariable Long id, // 简化：已全局配置-parameters编译参数
@Parameter(description = "${BUSINESS_NAME}更新参数") @Validated @RequestBody ${entity}UpdateRequest updateRequest
) {
log.info("【修改{}】ID：{}，请求参数：{}", BUSINESS_NAME, id, updateRequest);
return ${table.entityPath}Service.update(id, updateRequest);
}

/**
* 分页查询${BUSINESS_NAME}
*
* @param pageRequest 分页查询参数（页码/页大小/排序）
* @return 分页结果（含总数、列表数据）
*/
@GetMapping("page")
@Operation(summary = "分页查询${BUSINESS_NAME}")
public R${'<'}${entity}PageResponse${'>'} page(@Validated ${entity}PageRequest pageRequest) {
log.info("【分页查询{}】请求参数：{}", BUSINESS_NAME, pageRequest);
return ${table.entityPath}Service.page(pageRequest);
}

/**
* 查询${BUSINESS_NAME}列表（不分页）
*
* @return ${BUSINESS_NAME}完整列表
*/
@GetMapping
@Operation(summary = "查询${BUSINESS_NAME}列表")
public R${'<'}List${'<'}${entity}ListResponse${'>'}${'>'} list() {
log.info("【查询{}列表】请求", BUSINESS_NAME);
return ${table.entityPath}Service.list();
}

/**
* 查询${BUSINESS_NAME}详情
*
* @param id ${BUSINESS_NAME}ID
* @return ${BUSINESS_NAME}完整详情（含所有字段）
*/
@GetMapping("/{id}")
@Operation(summary = "查询${BUSINESS_NAME}详情")
public R${'<'}${entity}Vo${'>'} info(@PathVariable Long id) {
log.info("【查询{}详情】ID：{}", BUSINESS_NAME, id);
return ${table.entityPath}Service.info(id);
}
}