package com.zesheng.sys.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.zesheng.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统端-角色与权限关联
 * 说明：id/createdAt/updatedAt 已在 BaseEntity 中定义，此处仅生成业务字段
 *
 * @author czk
 * @since 2026-02-20
 */
@Getter
@Setter
@ToString
@TableName("sys_role_permission")
@Data
public class RolePermission {
    private static final long serialVersionUID = 1L;

    /**
     *  主键ID，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 创建时间
     * fill = FieldFill.INSERT：插入时自动填充
     */
    @TableField(value = "created_at", insertStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdAt;

    /**
     * 角色ID
     */
    @TableField("role_id")
    private Long roleId;

    /**
     * 权限ID
     */
    @TableField("permission_id")
    private Long permissionId;

}