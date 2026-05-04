package com.zesheng.sys.model.response;

import com.zesheng.common.response.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;

/**
* Role分页响应
*
* @author czk
* @since Thu Feb 19
*/
@Schema(name = "RolePageResponse", description = "系统端-角色表分页响应")
public class RolePageResponse extends PageResult<RoleVo> {
// 继承PageResult：包含pageMeta（分页元信息）和records（数据列表）
}