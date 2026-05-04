package com.zesheng.admin.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 帮助FAQ更新请求
 */
@Data
public class HelpFaqUpdateRequest {

    @NotBlank(message = "问题不能为空")
    private String question;

    @NotBlank(message = "答案不能为空")
    private String answer;

    private Integer sortOrder = 0;

    private Integer status = 1;
}
