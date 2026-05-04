package com.zesheng.sys.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
* RolePermission详情响应
* 说明：包含所有字段（业务字段+BaseEntity字段）
*
* @author czk
* @since Fri Feb 20
*/
@Data
@Schema(name = "RolePermissionVo", description = "系统端-权限表详情响应")
public class RolePermissionVo {
// BaseEntity字段（显式声明，便于Swagger文档展示）
@Schema(description = "主键ID", example = "1")
private Long id;

@Schema(description = "创建时间", example = "2026-02-18T16:00:00")
private LocalDateTime createdAt;

@Schema(description = "更新时间", example = "2026-02-18T16:00:00")
private LocalDateTime updatedAt;

// 业务字段

    @Schema(description = "角色ID", example = "1")
    private Long roleId;


    @Schema(description = "权限ID", example = "1")
    private Long permissionId;

}