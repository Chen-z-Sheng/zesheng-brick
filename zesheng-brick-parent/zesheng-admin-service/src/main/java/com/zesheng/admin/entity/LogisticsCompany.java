package com.zesheng.admin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zesheng.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("admin_logistics_company")
@Schema(description = "物流公司")
public class LogisticsCompany extends BaseEntity {

    private String name;

    private String code;

    private Integer sort;

    private Boolean status;
}
