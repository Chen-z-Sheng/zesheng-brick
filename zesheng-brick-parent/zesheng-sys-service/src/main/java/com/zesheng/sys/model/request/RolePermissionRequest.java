package com.zesheng.sys.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
* RolePermission通用请求参数
*
* @author czk
* @since Fri Feb 20
*/
@Data
@Schema(name = "RolePermissionRequest", description = "系统端-权限表通用请求参数")
public class RolePermissionRequest {


            @Schema(description = "角色ID", example = "1")
                    @NotNull(message = "角色ID不能为空")
            private Long roleId;


            @Schema(description = "权限ID", example = "1")
                    @NotNull(message = "权限ID不能为空")
            private Long permissionId;


}
