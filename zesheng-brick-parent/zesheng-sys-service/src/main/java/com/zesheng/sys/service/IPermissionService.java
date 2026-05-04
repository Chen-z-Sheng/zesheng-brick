package com.zesheng.sys.service;

import com.zesheng.sys.model.request.PermissionPageRequest;
import com.zesheng.sys.model.request.PermissionSaveRequest;
import com.zesheng.sys.model.request.PermissionUpdateRequest;
import com.zesheng.sys.model.response.PermissionListResponse;
import com.zesheng.sys.model.response.PermissionPageResponse;
import com.zesheng.sys.model.response.PermissionSaveResponse;
import com.zesheng.sys.model.response.PermissionUpdateResponse;
import com.zesheng.sys.model.response.PermissionVo;
import com.zesheng.common.response.R;

import java.util.List;

/**
* 系统端-权限表 服务类
*
* @author czk
* @since 2026-02-19
*/
public interface IPermissionService {
// 统一业务名称：避免重复替换
String BUSINESS_NAME = "系统端-权限表";

/**
* 新增系统端-权限表
*
* @param saveRequest 新增请求参数
* @return 新增结果（含ID、创建/更新时间）
*/
R<PermissionSaveResponse> save(PermissionSaveRequest saveRequest);

/**
* 批量删除系统端-权限表
*
* @param ids 要删除的ID列表
* @return 实际删除数量
*/
R<Integer> delete(List<Long> ids);

/**
* 修改系统端-权限表
*
* @param id           要修改的ID
* @param updateRequest 更新请求参数
* @return 更新结果（含最新更新时间）
*/
R<PermissionUpdateResponse> update(Long id, PermissionUpdateRequest updateRequest);

/**
* 分页查询系统端-权限表
*
* @param pageRequest 分页查询参数
* @return 分页结果
*/
R<PermissionPageResponse> page(PermissionPageRequest pageRequest);

/**
* 查询系统端-权限表列表（不分页）
*
* @return 列表数据
*/
R<List<PermissionListResponse>> list();

/**
* 查询系统端-权限表详情
*
* @param id 主键ID
* @return 详情数据
*/
R<PermissionVo> info(Long id);

/**
* 根据用户ID查询权限列表
*
* @param userId 用户ID
* @return 权限列表
*/
R<List<PermissionListResponse>> getPermissionsByUserId(Long userId);
}