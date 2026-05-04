package com.zesheng.sys.model.response;

import com.zesheng.common.response.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;

/**
* Permission分页响应
*
* @author czk
* @since Thu Feb 19
*/
@Schema(name = "PermissionPageResponse", description = "系统端-权限表分页响应")
public class PermissionPageResponse extends PageResult<PermissionVo> {
// 继承PageResult：包含pageMeta（分页元信息）和records（数据列表）
}