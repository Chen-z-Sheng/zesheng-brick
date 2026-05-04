package com.zesheng.admin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zesheng.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 回收品类二级分类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("admin_category_level2")
public class CategoryLevel2 extends BaseEntity {

    @TableField("level1_id")
    private Long level1Id;

    @TableField("name")
    private String name;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("status")
    private Integer status;
}
