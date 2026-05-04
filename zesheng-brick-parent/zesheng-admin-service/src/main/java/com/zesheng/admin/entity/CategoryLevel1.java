package com.zesheng.admin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zesheng.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 回收品类一级分类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("admin_category_level1")
public class CategoryLevel1 extends BaseEntity {

    @TableField("name")
    private String name;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("status")
    private Integer status;
}
