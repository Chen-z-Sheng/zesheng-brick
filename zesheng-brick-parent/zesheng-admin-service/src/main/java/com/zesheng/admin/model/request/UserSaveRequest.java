package com.zesheng.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "UserSaveRequest", description = "用户表新增请求参数")
public class UserSaveRequest{
    @Schema(description = "密码")
    @NotBlank
    private String password;

    @Schema(description = "用户名")
    @NotBlank
    private String username;

    @Schema(description = "所属角色ID（单用户单角色）")
    private Long roleId;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "头像链接")
    private String avatarUrl;

    @Schema(description = "账号状态：1=启用，0=禁用")
    private Integer status;

    @Schema(description = "创建人ID")
    private Long createBy;

    @Schema(description = "最后更新人ID")
    private Long updateBy;

    @Schema(description = "最后登录时间")
    private java.time.LocalDateTime lastLoginAt;

    @Schema(description = "最后登录IP")
    private String lastLoginIp;

    @Schema(description = "备注")
    private String remark;

}