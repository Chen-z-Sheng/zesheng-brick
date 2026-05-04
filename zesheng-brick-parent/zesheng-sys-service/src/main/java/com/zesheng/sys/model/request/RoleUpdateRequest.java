package com.zesheng.sys.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
* Role更新请求参数
* 说明：更新不做非空校验（支持部分字段修改）
*
* @author czk
* @since Thu Feb 19
*/
@Data
@Schema(name = "RoleUpdateRequest", description = "系统端-角色表更新请求参数")
public class RoleUpdateRequest {


            @Schema(description = "角色名称（用于界面显示）", example = "示例值")
            private String name;


            @Schema(description = "角色编码（英文/下划线）", example = "示例值")
            private String code;


            @Schema(description = "状态：1=启用，0=禁用", example = "true")
            private Integer status;


            @Schema(description = "备注/说明", example = "示例值")
            private String description;

    @Schema(description = "权限码列表，更新时先删该角色原有关联再按此列表重建")
    private java.util.List<String> permissionCodes;

}