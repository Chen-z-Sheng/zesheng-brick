package com.zesheng.sys.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Permission详情响应
 * 说明：包含所有字段（业务字段+BaseEntity字段）
 *
 * @author czk
 * @since Thu Feb 19
 */
@Data
@Schema(name = "PermissionVo", description = "系统端-权限表详情响应")
public class PermissionVo {
    // BaseEntity字段（显式声明，便于Swagger文档展示）
    @Schema(description = "主键ID", example = "1")
    private Long id;

    @Schema(description = "创建时间", example = "2026-02-18T16:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2026-02-18T16:00:00")
    private LocalDateTime updatedAt;

// 业务字段

    @Schema(description = "权限码：<资源>:<动作>，如 user:create；唯一", example = "示例值")
    private String code;


    @Schema(description = "资源名/模块名，如 user/order/form", example = "示例值")
    private String resource;


    @Schema(description = "动作，如 list/read/create/update/delete/export/approve", example = "示例值")
    private String action;


    @Schema(description = "权限点说明（用于回显/帮助）", example = "示例值")
    private String description;

}