package com.zesheng.sys.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
* RolePermission新增响应
*
* @author czk
* @since Fri Feb 20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "RolePermissionSaveResponse", description = "系统端-权限表新增响应")
public class RolePermissionSaveResponse extends RolePermissionVo {
// ID已在Vo中声明，此处无需重复定义（若需显式可保留，建议复用Vo）
@Schema(description = "系统端-权限表主键ID", example = "6")
private Long id;
}