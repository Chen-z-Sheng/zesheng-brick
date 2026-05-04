package com.zesheng.client.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zesheng.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 物流公司字典表（与 admin 共用库表 admin_logistics_company，C 端仅只读查询）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("admin_logistics_company")
public class LogisticsCompany extends BaseEntity {

    private String name;

    private String code;

    private Integer sort;

    private Boolean status;
}
