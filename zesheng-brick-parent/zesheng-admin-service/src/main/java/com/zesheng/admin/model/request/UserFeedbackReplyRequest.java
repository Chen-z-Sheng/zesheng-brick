package com.zesheng.admin.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户反馈回复请求
 */
@Data
public class UserFeedbackReplyRequest {

    @NotBlank(message = "回复内容不能为空")
    private String replyContent;
}
