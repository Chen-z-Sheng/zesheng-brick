package com.zesheng.common.enums;

import lombok.Getter;

import java.io.Serializable;

/**
 * 系统配置值类型枚举
 */
@Getter
public enum ConfigValueTypeEnum implements Serializable {
    JSON("json", "JSON类型"),
    STRING("string", "字符串类型"),
    NUMBER("number", "数字类型"),
    BOOLEAN("boolean", "布尔类型");

    private static final long serialVersionUID = 1L;
    private final String code;
    private final String desc;

    ConfigValueTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ConfigValueTypeEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        if ("bool".equalsIgnoreCase(code.trim())) {
            return BOOLEAN;
        }
        for (ConfigValueTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的配置值类型：" + code);
    }
}