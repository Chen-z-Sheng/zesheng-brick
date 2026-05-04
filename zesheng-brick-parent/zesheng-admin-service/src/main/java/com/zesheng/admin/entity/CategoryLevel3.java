package com.zesheng.admin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zesheng.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 回收品类三级分类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("admin_category_level3")
public class CategoryLevel3 extends BaseEntity {

    @TableField("level2_id")
    private Long level2Id;

    @TableField("name")
    private String name;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("status")
    private Integer status;
}
