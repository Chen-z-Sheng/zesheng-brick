package com.zesheng.sys.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
* Permission更新请求参数
* 说明：更新不做非空校验（支持部分字段修改）
*
* @author czk
* @since Thu Feb 19
*/
@Data
@Schema(name = "PermissionUpdateRequest", description = "系统端-权限表更新请求参数")
public class PermissionUpdateRequest {


            @Schema(description = "权限码：<资源>:<动作>，如 user:create；唯一", example = "示例值")
            private String code;


            @Schema(description = "资源名/模块名，如 user/order/form", example = "示例值")
            private String resource;


            @Schema(description = "动作，如 list/read/create/update/delete/export/approve", example = "示例值")
            private String action;


            @Schema(description = "权限点说明（用于回显/帮助）", example = "示例值")
            private String description;

}