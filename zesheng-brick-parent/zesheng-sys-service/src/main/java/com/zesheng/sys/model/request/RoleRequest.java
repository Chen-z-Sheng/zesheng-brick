package com.zesheng.sys.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Role通用请求参数
 *
 * @author czk
 * @since Thu Feb 19
 */
@Data
@Schema(name = "RoleRequest", description = "系统端-角色表通用请求参数")
public class RoleRequest {


    @Schema(description = "角色名称（用于界面显示）", example = "示例值")
    @NotBlank(message = "角色名称（用于界面显示）不能为空")
    private String name;


    @Schema(description = "角色编码（英文/下划线）", example = "示例值")
    @NotBlank(message = "角色编码（英文/下划线）不能为空")
    private String code;


    @Schema(description = "状态：1=启用，0=禁用", example = "true")
    @NotNull(message = "状态：1=启用，0=禁用不能为空")
    private Integer status;


    @Schema(description = "备注/说明", example = "示例值")
    private String description;


}
