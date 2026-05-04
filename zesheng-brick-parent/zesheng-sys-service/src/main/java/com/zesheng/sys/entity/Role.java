package com.zesheng.sys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.zesheng.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统端-角色表
 * 说明：id/createdAt/updatedAt 已在 BaseEntity 中定义，此处仅生成业务字段
 *
 * @author czk
 * @since 2026-02-19
 */
@TableName("sys_role")
@Data
@EqualsAndHashCode(callSuper = true) // 继承BaseEntity必须开启
public class Role extends BaseEntity {
    private static final long serialVersionUID = 1L;


    /**
     * 角色名称（用于界面显示）
     */
    @TableField("name")
    private String name;

    /**
     * 角色编码（英文/下划线）
     */
    @TableField("code")
    private String code;

    /**
     * 状态：1=启用，0=禁用
     */
    @TableField("status")
    private Byte status;

    /**
     * 备注/说明
     */
    @TableField("description")
    private String description;

}