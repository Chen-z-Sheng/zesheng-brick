package com.zesheng.sys.model.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
* Permission新增请求参数
* 说明：继承通用Request，复用非空校验规则
*
* @author czk
* @since Thu Feb 19
*/
@Schema(name = "PermissionSaveRequest", description = "系统端-权限表新增请求参数")
public class PermissionSaveRequest extends PermissionRequest {
// 仅继承通用Request，无额外字段
}