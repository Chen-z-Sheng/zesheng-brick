package com.zesheng.sys.service;

import com.zesheng.common.response.BatchDeleteResponse;
import com.zesheng.sys.model.request.RolePermissionPageRequest;
import com.zesheng.sys.model.request.RolePermissionSaveRequest;
import com.zesheng.sys.model.request.RolePermissionUpdateRequest;
import com.zesheng.sys.model.response.RolePermissionListResponse;
import com.zesheng.sys.model.response.RolePermissionPageResponse;
import com.zesheng.sys.model.response.RolePermissionSaveResponse;
import com.zesheng.sys.model.response.RolePermissionUpdateResponse;
import com.zesheng.sys.model.response.RolePermissionVo;
import com.zesheng.common.response.R;

import java.util.List;

/**
* 系统端-角色与权限关联 服务类
*
* @author czk
* @since 2026-02-20
*/
public interface IRolePermissionService {
// 统一业务名称：避免重复替换
String BUSINESS_NAME = "系统端-角色与权限关联";

/**
* 新增系统端-角色与权限关联
*
* @param saveRequest 新增请求参数
* @return 新增结果（含ID、创建/更新时间）
*/
R<RolePermissionSaveResponse> save(RolePermissionSaveRequest saveRequest);

/**
* 单个删除系统端-角色与权限关联
*
* @param id 要删除的ID
* @return 删除结果
*/
R<Void> delete(Long id);

/**
* 批量删除系统端-角色与权限关联
*
* @param ids 要删除的ID列表
* @return 批量删除详细结果
*/
R<BatchDeleteResponse> delete(List<Long> ids);

/**
* 修改系统端-角色与权限关联
*
* @param id           要修改的ID
* @param updateRequest 更新请求参数
* @return 更新结果（含最新更新时间）
*/
R<RolePermissionUpdateResponse> update(Long id, RolePermissionUpdateRequest updateRequest);

/**
* 分页查询系统端-角色与权限关联
*
* @param pageRequest 分页查询参数
* @return 分页结果
*/
R<RolePermissionPageResponse> page(RolePermissionPageRequest pageRequest);

/**
* 查询系统端-角色与权限关联列表（不分页）
*
* @return 列表数据
*/
R<List<RolePermissionListResponse>> list();

/**
* 查询系统端-角色与权限关联详情
*
* @param id 主键ID
* @return 详情数据
*/
R<RolePermissionVo> info(Long id);
}