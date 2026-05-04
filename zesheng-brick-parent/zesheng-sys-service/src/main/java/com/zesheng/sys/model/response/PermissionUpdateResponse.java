package com.zesheng.sys.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
* Permission更新响应
*
* @author czk
* @since Thu Feb 19
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "PermissionUpdateResponse", description = "系统端-权限表更新响应")
public class PermissionUpdateResponse extends PermissionVo {
// ID已在Vo中声明，此处无需重复定义（若需显式可保留）
@Schema(description = "系统端-权限表主键ID", example = "6")
private Long id;
}