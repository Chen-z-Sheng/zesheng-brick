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
 * 系统端-权限表
 * 说明：id/createdAt/updatedAt 已在 BaseEntity 中定义，此处仅生成业务字段
 *
 * @author czk
 * @since 2026-02-19
 */
@Getter
@Setter
@ToString
@TableName("sys_permission")
@Data
@EqualsAndHashCode(callSuper = true) // 继承BaseEntity必须开启
public class Permission extends BaseEntity {
    private static final long serialVersionUID = 1L;


    /**
     * 权限码：<资源>:<动作>，如 user:create；唯一
     */
    @TableField("code")
    private String code;

    /**
     * 资源名/模块名，如 user/order/form
     */
    @TableField("resource")
    private String resource;

    /**
     * 动作，如 list/read/create/update/delete/export/approve
     */
    @TableField("action")
    private String action;

    /**
     * 权限点说明（用于回显/帮助）
     */
    @TableField("description")
    private String description;

}