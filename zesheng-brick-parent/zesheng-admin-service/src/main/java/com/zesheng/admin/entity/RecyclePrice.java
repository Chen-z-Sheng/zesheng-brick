package com.zesheng.admin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zesheng.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 回收行情记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("admin_recycle_price")
public class RecyclePrice extends BaseEntity {

    @TableField("level3_id")
    private Long level3Id;

    @TableField("price_date")
    private LocalDate priceDate;

    @TableField("recycle_price")
    private BigDecimal recyclePrice;

    @TableField("remark")
    private String remark;
}
