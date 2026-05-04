package com.zesheng.admin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zesheng.common.entity.BaseEntity;
import com.zesheng.common.enums.StatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理端-用户表
 *
 * @author czk
 * @since 2026-02-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("admin_user")
public class User extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 密码哈希
     */
    @TableField("password_hash")
    private String passwordHash;

    /**
     * 所属角色ID（单用户单角色）
     */
    @TableField("role_id")
    private Long roleId;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 头像链接
     */
    @TableField("avatar_url")
    private String avatarUrl;

    /**
     * 账号状态：1=启用，0=禁用
     */
    @TableField("status")
    private StatusEnum status;

    /**
     * 创建人ID
     */
    @TableField("create_by")
    private Long createBy;

    /**
     * 最后更新人ID
     */
    @TableField("update_by")
    private Long updateBy;

    /**
     * 最后登录时间
     */
    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * 最后登录IP
     */
    @TableField("last_login_ip")
    private String lastLoginIp;

    /**
     * 软删除时间
     */
    @TableField("deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
}