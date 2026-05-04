package com.zesheng.sys.model.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
* RolePermission列表响应
* 说明：继承Vo，复用所有字段
*
* @author czk
* @since Fri Feb 20
*/
@Schema(name = "RolePermissionListResponse", description = "系统端-权限表列表响应")
public class RolePermissionListResponse extends RolePermissionVo {
// 仅继承Vo，无额外字段
}