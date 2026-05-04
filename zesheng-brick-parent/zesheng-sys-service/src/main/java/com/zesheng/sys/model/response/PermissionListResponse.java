package com.zesheng.sys.model.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
* Permission列表响应
* 说明：继承Vo，复用所有字段
*
* @author czk
* @since Thu Feb 19
*/
@Schema(name = "PermissionListResponse", description = "系统端-权限表列表响应")
public class PermissionListResponse extends PermissionVo {
// 仅继承Vo，无额外字段
}