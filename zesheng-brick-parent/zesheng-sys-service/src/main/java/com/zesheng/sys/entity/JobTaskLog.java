package com.zesheng.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zesheng.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_job_task_log")
public class JobTaskLog extends BaseEntity {

    @TableField("task_id")
    private Long taskId;

    @TableField("job_name")
    private String jobName;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("status")
    private Integer status;

    @TableField("message")
    private String message;

    @TableField("error_stack")
    private String errorStack;
}
