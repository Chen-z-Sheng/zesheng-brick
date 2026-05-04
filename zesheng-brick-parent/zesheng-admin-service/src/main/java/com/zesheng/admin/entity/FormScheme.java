package com.zesheng.admin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zesheng.common.entity.BaseEntity;
import com.zesheng.common.enums.StatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "admin_form_schemes")
@Schema(description = "管理端-方案表")
public class FormScheme extends BaseEntity {

    /**
     * 方案名称
     */
    private String name;

    /**
     * 关联下单地址ID
     */
    private Long addressId;

    /**
     * 每单结算金额
     */
    private BigDecimal unitPrice;

    /**
     * 方案说明
     */
    private String description;

    /**
     * 状态：0=停用 1=启用 2=草稿
     */
    private StatusEnum status;

}
