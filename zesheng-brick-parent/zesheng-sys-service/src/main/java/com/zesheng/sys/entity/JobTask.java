package com.zesheng.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zesheng.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_job_task")
public class JobTask extends BaseEntity {

    @TableField("job_name")
    private String jobName;

    @TableField("cron_expression")
    private String cronExpression;

    @TableField("handler_name")
    private String handlerName;

    @TableField("handler_param")
    private String handlerParam;

    @TableField("status")
    private Integer status;

    @TableField("running")
    private Integer running;

    @TableField("last_execute_at")
    private LocalDateTime lastExecuteAt;

    @TableField("next_execute_at")
    private LocalDateTime nextExecuteAt;

    @TableField("last_execute_status")
    private Integer lastExecuteStatus;

    @TableField("last_execute_message")
    private String lastExecuteMessage;
}
