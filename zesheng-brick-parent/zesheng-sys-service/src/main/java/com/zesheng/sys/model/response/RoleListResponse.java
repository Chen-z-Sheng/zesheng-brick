package com.zesheng.sys.model.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
* Role列表响应
* 说明：继承Vo，复用所有字段
*
* @author czk
* @since Thu Feb 19
*/
@Schema(name = "RoleListResponse", description = "系统端-角色表列表响应")
public class RoleListResponse extends RoleVo {
// 仅继承Vo，无额外字段
}