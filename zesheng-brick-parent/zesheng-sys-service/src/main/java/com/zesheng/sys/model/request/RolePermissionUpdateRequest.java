package com.zesheng.sys.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
* RolePermission更新请求参数
* 说明：更新不做非空校验（支持部分字段修改）
*
* @author czk
* @since Fri Feb 20
*/
@Data
@Schema(name = "RolePermissionUpdateRequest", description = "系统端-权限表更新请求参数")
public class RolePermissionUpdateRequest {


            @Schema(description = "角色ID", example = "1")
            private Long roleId;


            @Schema(description = "权限ID", example = "1")
            private Long permissionId;

}