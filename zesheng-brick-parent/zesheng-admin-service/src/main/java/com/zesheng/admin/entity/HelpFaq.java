package com.zesheng.admin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zesheng.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 帮助中心FAQ（小程序常见问题）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("admin_help_faq")
public class HelpFaq extends BaseEntity {

    @TableField("question")
    private String question;

    @TableField("answer")
    private String answer;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("status")
    private Integer status;
}
