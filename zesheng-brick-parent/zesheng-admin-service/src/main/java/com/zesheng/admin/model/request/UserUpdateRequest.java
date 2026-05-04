package com.zesheng.admin.model.request;

import com.zesheng.common.enums.StatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(name = "UserUpdateRequest", description = "用户表更新请求参数")
public class UserUpdateRequest {

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "所属角色ID（单用户单角色）")
    private Long roleId;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "头像链接")
    private String avatarUrl;

    @Schema(description = "账号状态：1=启用，0=禁用", allowableValues = {"0", "1"}, example = "1")
    private StatusEnum status;

    @Schema(description = "创建人ID")
    private Long createdBy;

    @Schema(description = "最后更新人ID")
    private Long updatedBy;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @Schema(description = "最后登录IP")
    private String lastLoginIp;

    @Schema(description = "备注")
    private String remark;


}
