package com.zesheng.client.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zesheng.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 公告表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("admin_announcement")
@Schema(description = "公告")
public class Announcement extends BaseEntity {

    @TableField("title")
    @Schema(description = "标题")
    private String title;

    @TableField("content")
    @Schema(description = "内容（富文本HTML）")
    private String content;

    @TableField("status")
    @Schema(description = "状态：1=启用，0=未启用")
    private Integer status;
}
