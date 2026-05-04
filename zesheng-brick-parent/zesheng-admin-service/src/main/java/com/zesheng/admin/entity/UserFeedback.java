package com.zesheng.admin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.zesheng.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端-用户问题反馈
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "admin_user_feedback", autoResultMap = true)
@Schema(description = "管理端-用户问题反馈")
public class UserFeedback extends BaseEntity {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "反馈类型")
    private String feedbackType;

    @Schema(description = "反馈内容")
    private String content;

    @TableField(value = "image_urls", typeHandler = JacksonTypeHandler.class)
    @Schema(description = "图片URL列表")
    private List<String> imageUrls;

    @Schema(description = "状态：0待处理 1已处理")
    private Integer status;

    @Schema(description = "管理员回复内容")
    private String replyContent;

    @Schema(description = "回复时间")
    private LocalDateTime repliedAt;

    @Schema(description = "回复管理员ID")
    private Long replyBy;

    @Schema(description = "软删除时间")
    private LocalDateTime deletedAt;

    @TableField(exist = false)
    @Schema(description = "用户展示名")
    private String displayName;
}
