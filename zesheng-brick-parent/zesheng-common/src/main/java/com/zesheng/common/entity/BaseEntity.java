package com.zesheng.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BaseEntity {
    /**
     *  主键ID，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 创建时间
     * fill = FieldFill.INSERT：插入时自动填充
     */
    @TableField(value = "created_at", insertStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     * fill = FieldFill.INSERT_UPDATE：插入/更新时自动填充
     */
    @TableField(value = "updated_at", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updatedAt;
}
