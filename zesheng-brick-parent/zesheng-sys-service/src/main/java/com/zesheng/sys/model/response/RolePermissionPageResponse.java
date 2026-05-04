package com.zesheng.sys.model.response;

import com.zesheng.common.response.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;

/**
* RolePermission分页响应
*
* @author czk
* @since Fri Feb 20
*/
@Schema(name = "RolePermissionPageResponse", description = "系统端-权限表分页响应")
public class RolePermissionPageResponse extends PageResult<RolePermissionVo> {
// 继承PageResult：包含pageMeta（分页元信息）和records（数据列表）
}