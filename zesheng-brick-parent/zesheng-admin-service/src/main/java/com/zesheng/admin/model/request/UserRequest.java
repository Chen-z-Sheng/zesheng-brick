package com.zesheng.admin.model.request;

import com.zesheng.common.enums.StatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(name = "UserRequest", description = "管理端-用户表通用请求参数")
public class UserRequest {

    @Schema(description = "用户名")
    @NotBlank
    private String username;

    @Schema(description = "所属角色ID（单用户单角色）")
    private Long roleId;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "头像链接")
    @NotBlank
    private String avatarUrl;

    @Schema(description = "账号状态：1=启用，0=禁用")
    @NotNull
    private StatusEnum status;

    @Schema(description = "创建人ID")
    @NotNull
    private Long createBy;

    @Schema(description = "最后更新人ID")
    @NotNull
    private Long updateBy;

    @Schema(description = "最后登录时间")
    private java.time.LocalDateTime lastLoginAt;

    @Schema(description = "最后登录IP")
    private String lastLoginIp;

    @Schema(description = "备注")
    private String remark;


}