package com.zesheng.client.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zesheng.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统配置表 sys_config，与后台 sys_config 共用同一张表
 */
@Data
@TableName("sys_config")
@EqualsAndHashCode(callSuper = true)
public class SysConfig extends BaseEntity {

    @TableField("`key`")
    private String configKey;

    private String value;

    private String valueType;

    private String remark;
}
