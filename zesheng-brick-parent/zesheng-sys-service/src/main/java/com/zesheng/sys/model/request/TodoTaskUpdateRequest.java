package com.zesheng.sys.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "待办任务更新请求参数")
public class TodoTaskUpdateRequest {

    @NotBlank(message = "任务标题不能为空")
    @Schema(description = "任务标题")
    private String title;

    @Schema(description = "任务内容")
    private String content;

    @Min(value = 0, message = "状态值非法")
    @Max(value = 1, message = "状态值非法")
    @Schema(description = "任务状态：0=待处理，1=已处理", example = "0")
    private Integer status;
}
