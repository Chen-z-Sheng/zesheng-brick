package com.zesheng.client.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zesheng.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 公告忽略记录（用户选择“不再弹窗”时写入）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("admin_announcement_ignore")
@Schema(description = "公告忽略记录")
public class AnnouncementIgnore extends BaseEntity {

    @TableField("announcement_id")
    @Schema(description = "公告ID")
    private Long announcementId;

    @TableField("openid")
    @Schema(description = "用户openid")
    private String openid;
}
