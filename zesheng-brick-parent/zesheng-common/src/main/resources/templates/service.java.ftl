<#assign BUSINESS_NAME = table.comment!?replace('管理端-','')?replace('管理端','')>
package ${package.Service};

import com.zesheng.common.response.BatchDeleteResponse;
import com.zesheng.${package.ModuleName}.model.request.${entity}PageRequest;
import com.zesheng.${package.ModuleName}.model.request.${entity}SaveRequest;
import com.zesheng.${package.ModuleName}.model.request.${entity}UpdateRequest;
import com.zesheng.${package.ModuleName}.model.response.${entity}ListResponse;
import com.zesheng.${package.ModuleName}.model.response.${entity}PageResponse;
import com.zesheng.${package.ModuleName}.model.response.${entity}SaveResponse;
import com.zesheng.${package.ModuleName}.model.response.${entity}UpdateResponse;
import com.zesheng.${package.ModuleName}.model.response.${entity}Vo;
import com.zesheng.common.response.R;

import java.util.List;

/**
* ${table.comment!?replace('管理端-','')?replace('管理端','')} 服务类
*
* @author ${author}
* @since ${date}
*/
public interface ${table.serviceName} {
// 统一业务名称：避免重复替换
String BUSINESS_NAME = "${table.comment!?replace('管理端-','')?replace('管理端','')}";

/**
* 新增${table.comment!?replace('管理端-','')?replace('管理端','')}
*
* @param saveRequest 新增请求参数
* @return 新增结果（含ID、创建/更新时间）
*/
R${'<'}${entity}SaveResponse${'>'} save(${entity}SaveRequest saveRequest);

/**
* 单个删除${table.comment!?replace('管理端-','')?replace('管理端','')}
*
* @param id 要删除的ID
* @return 删除结果
*/
R<Void> delete(Long id);

/**
* 批量删除${table.comment!?replace('管理端-','')?replace('管理端','')}
*
* @param ids 要删除的ID列表
* @return 批量删除详细结果
*/
R<BatchDeleteResponse> delete(List<Long> ids);

/**
* 修改${table.comment!?replace('管理端-','')?replace('管理端','')}
*
* @param id           要修改的ID
* @param updateRequest 更新请求参数
* @return 更新结果（含最新更新时间）
*/
R${'<'}${entity}UpdateResponse${'>'} update(Long id, ${entity}UpdateRequest updateRequest);

/**
* 分页查询${table.comment!?replace('管理端-','')?replace('管理端','')}
*
* @param pageRequest 分页查询参数
* @return 分页结果
*/
R${'<'}${entity}PageResponse${'>'} page(${entity}PageRequest pageRequest);

/**
* 查询${table.comment!?replace('管理端-','')?replace('管理端','')}列表（不分页）
*
* @return 列表数据
*/
R${'<'}List${'<'}${entity}ListResponse${'>'}${'>'} list();

/**
* 查询${BUSINESS_NAME}详情
*
* @param id 主键ID
* @return 详情数据
*/
R${'<'}${entity}Vo${'>'} info(Long id);
}