package com.zesheng.admin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zesheng.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 公告表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("admin_announcement")
public class Announcement extends BaseEntity {

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("status")
    private Integer status;
}
