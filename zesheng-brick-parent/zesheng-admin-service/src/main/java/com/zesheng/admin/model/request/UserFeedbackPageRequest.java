package com.zesheng.admin.model.request;

import com.zesheng.common.request.PageAndSortQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户问题反馈分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户问题反馈分页查询")
public class UserFeedbackPageRequest extends PageAndSortQueryRequest {

    @Schema(description = "反馈类型")
    private String feedbackType;

    @Schema(description = "用户ID")
    private Long userId;
}
