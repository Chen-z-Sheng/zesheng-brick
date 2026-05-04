package com.zesheng.sys.model.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
* RolePermission新增请求参数
* 说明：继承通用Request，复用非空校验规则
*
* @author czk
* @since Fri Feb 20
*/
@Schema(name = "RolePermissionSaveRequest", description = "系统端-权限表新增请求参数")
public class RolePermissionSaveRequest extends RolePermissionRequest {
// 仅继承通用Request，无额外字段
}