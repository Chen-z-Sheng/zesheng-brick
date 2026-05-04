package com.zesheng.sys.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Permission通用请求参数
 *
 * @author czk
 * @since Thu Feb 19
 */
@Data
@Schema(name = "PermissionRequest", description = "系统端-权限表通用请求参数")
public class PermissionRequest {


    @Schema(description = "权限码：<资源>:<动作>，如 user:create；唯一", example = "示例值")
    @NotBlank(message = "权限码：<资源>:<动作>，如 user:create；唯一不能为空")
    private String code;


    @Schema(description = "资源名/模块名，如 user/order/form", example = "示例值")
    @NotBlank(message = "资源名/模块名，如 user/order/form不能为空")
    private String resource;


    @Schema(description = "动作，如 list/read/create/update/delete/export/approve", example = "示例值")
    @NotBlank(message = "动作，如 list/read/create/update/delete/export/approve不能为空")
    private String action;


    @Schema(description = "权限点说明（用于回显/帮助）", example = "示例值")
    private String description;


}
