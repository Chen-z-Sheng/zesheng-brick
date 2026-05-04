package com.zesheng.sys.service;

import com.zesheng.sys.model.request.RolePageRequest;
import com.zesheng.sys.model.request.RoleSaveRequest;
import com.zesheng.sys.model.request.RoleUpdateRequest;
import com.zesheng.sys.model.response.RoleListResponse;
import com.zesheng.sys.model.response.RolePageResponse;
import com.zesheng.sys.model.response.RoleSaveResponse;
import com.zesheng.sys.model.response.RoleUpdateResponse;
import com.zesheng.sys.model.response.RoleVo;
import com.zesheng.common.response.R;

import java.util.List;

/**
 * 系统端-角色表 服务类
 *
 * @author czk
 * @since 2026-02-19
 */
public interface IRoleService {
    // 统一业务名称：避免重复替换
    String BUSINESS_NAME = "系统端-角色表";

    /**
     * 新增系统端-角色表
     *
     * @param saveRequest 新增请求参数
     * @return 新增结果（含ID、创建/更新时间）
     */
    R<RoleSaveResponse> save(RoleSaveRequest saveRequest);

    /**
     * 批量删除系统端-角色表
     *
     * @param ids 要删除的ID列表
     * @return 实际删除数量
     */
    R<Integer> delete(List<Long> ids);

    /**
     * 修改系统端-角色表
     *
     * @param id            要修改的ID
     * @param updateRequest 更新请求参数
     * @return 更新结果（含最新更新时间）
     */
    R<RoleUpdateResponse> update(Long id, RoleUpdateRequest updateRequest);

    /**
     * 分页查询系统端-角色表
     *
     * @param pageRequest 分页查询参数
     * @return 分页结果
     */
    R<RolePageResponse> page(RolePageRequest pageRequest);

    /**
     * 查询系统端-角色表列表（不分页）
     *
     * @return 列表数据
     */
    R<List<RoleListResponse>> list();

    /**
     * 查询系统端-角色表详情
     *
     * @param id 主键ID
     * @return 详情数据
     */
    R<RoleVo> info(Long id);
}