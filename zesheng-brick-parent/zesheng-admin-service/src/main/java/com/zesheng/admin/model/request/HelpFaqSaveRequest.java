package com.zesheng.admin.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 帮助FAQ新增请求
 */
@Data
public class HelpFaqSaveRequest {

    @NotBlank(message = "问题不能为空")
    private String question;

    @NotBlank(message = "答案不能为空")
    private String answer;

    @NotNull(message = "排序号不能为空")
    private Integer sortOrder = 0;

    @NotNull(message = "状态不能为空")
    private Integer status = 1;
}
