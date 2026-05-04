package com.zesheng.sys.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JobTaskSaveRequest {

    @NotBlank(message = "任务名称不能为空")
    private String jobName;

    @NotBlank(message = "Cron表达式不能为空")
    private String cronExpression;

    @NotBlank(message = "执行器名称不能为空")
    private String handlerName;

    private String handlerParam;

    @NotNull(message = "任务状态不能为空")
    private Integer status;
}
