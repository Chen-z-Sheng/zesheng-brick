package com.zesheng.admin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zesheng.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "admin_delivery_addresses")
@Schema(description = "管理端-下单地址")
public class DeliveryAddress extends BaseEntity {

    private String name;

    @TableField("full_address")
    private String fullAddress;

    private Integer sortOrder = 0;
    private Integer status = 1;
}
