package com.zesheng.admin.model.response;

import com.zesheng.common.enums.StatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "UserVo", description = "用户表详情响应")
public class UserVo {

    @Schema(name = "主键ID")
    private Long id;

    @Schema(name = "用户名")
    private String username;

    @Schema(name = "所属角色ID（单用户单角色）")
    private Long roleId;

    @Schema(name = "角色编码，如 admin，用于前端权限兼容")
    private String roleCode;

    @Schema(name = "手机号")
    private String phone;

    @Schema(name = "头像链接")
    private String avatarUrl;

    @Schema(name = "账号状态：1=启用，0=禁用")
    private StatusEnum status;

    @Schema(name = "创建人ID")
    private Long createBy;

    @Schema(name = "最后更新人ID")
    private Long updateBy;

    @Schema(name = "最后登录时间")
    private java.time.LocalDateTime lastLoginAt;

    @Schema(name = "最后登录IP")
    private String lastLoginIp;

    @Schema(name = "软删除时间")
    private java.time.LocalDateTime deletedAt;

    @Schema(name = "创建时间")
    private java.time.LocalDateTime createdAt;

    @Schema(name = "更新时间")
    private java.time.LocalDateTime updatedAt;

    @Schema(name = "备注")
    private String remark;


}