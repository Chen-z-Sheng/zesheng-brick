package com.zesheng.client.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zesheng.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 帮助中心FAQ
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("admin_help_faq")
@Schema(description = "帮助中心FAQ")
public class HelpFaq extends BaseEntity {

    @TableField("question")
    @Schema(description = "问题")
    private String question;

    @TableField("answer")
    @Schema(description = "答案")
    private String answer;

    @TableField("sort_order")
    @Schema(description = "排序号")
    private Integer sortOrder;

    @TableField("status")
    @Schema(description = "状态：1=启用，0=禁用")
    private Integer status;
}
