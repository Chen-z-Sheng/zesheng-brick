package com.zesheng.sys.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Role详情响应
 * 说明：包含所有字段（业务字段+BaseEntity字段）
 *
 * @author czk
 * @since Thu Feb 19
 */
@Data
@Schema(name = "RoleVo", description = "系统端-角色表详情响应")
public class RoleVo {
    // BaseEntity字段（显式声明，便于Swagger文档展示）
    @Schema(description = "主键ID", example = "1")
    private Long id;

    @Schema(description = "创建时间", example = "2026-02-18T16:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2026-02-18T16:00:00")
    private LocalDateTime updatedAt;

    // 业务字段

    @Schema(description = "角色名称（用于界面显示）", example = "示例值")
    private String name;


    @Schema(description = "角色编码（英文/下划线）", example = "示例值")
    private String code;


    @Schema(description = "状态：1=启用，0=禁用", example = "true")
    private Integer status;


    @Schema(description = "备注/说明", example = "示例值")
    private String description;

    @Schema(description = "权限码列表（列表/详情接口填充，用于前端回显）")
    private java.util.List<String> permissionCodes;

}