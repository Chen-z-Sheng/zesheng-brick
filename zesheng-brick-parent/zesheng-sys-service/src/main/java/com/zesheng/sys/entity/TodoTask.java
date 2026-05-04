package com.zesheng.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zesheng.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_todo_task")
public class TodoTask extends BaseEntity {

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("status")
    private Integer status;
}
