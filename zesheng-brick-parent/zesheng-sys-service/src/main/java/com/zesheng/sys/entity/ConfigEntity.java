package com.zesheng.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zesheng.common.entity.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("sys_config")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "系统配置实体")
public class ConfigEntity extends BaseEntity {
    /**
     * 配置键，对应数据库字段：`key`（保留字）
     */
    @TableField("`key`")
    @Schema(description = "配置键")
    private String configKey;

    /**
     * 配置值原文（文本存储）；解析方式由 valueType 决定，见 {@link com.zesheng.common.util.ConfigValueParser}
     */
    @Schema(description = "配置值")
    private String value;

    @Schema(description = "配置值类型")
    private String valueType;

    @Schema(description = "配置备注")
    private String remark;
}
