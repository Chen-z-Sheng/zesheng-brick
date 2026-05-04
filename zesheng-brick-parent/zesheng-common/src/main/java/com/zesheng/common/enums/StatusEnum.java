package com.zesheng.common.enums;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.io.Serializable;

/**
 * 系统通用状态枚举（整合全系统所有二元状态，避免枚举泛滥）
 * 适用场景：账号、配置、方案、商品等模块的“启用/禁用”“是/否”等通用状态
 */
@Getter
public enum StatusEnum implements IEnum<Integer> {

    // ========== 基础启用/禁用 ==========
    DISABLE(0, "禁用"),

    ENABLE(1, "启用"),

    DRAFT(2, "草稿"),

    // ========== 扩展通用状态 ==========
    NO(0, "否"),          // 通用“否”（比如是否删除、是否默认）

    YES(1, "是"),         // 通用“是”

    NORMAL(1, "正常"),    // 通用“正常”（比如订单、商品状态）

    ABNORMAL(0, "异常");  // 通用“异常”


    // 序列化版本号
    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 状态描述
     */
    private final String desc;

    // 构造器
    StatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据状态码获取枚举值
     */
    public static StatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (StatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的通用状态码：" + code);
    }

    /**
     * 判断是否为启用状态
     */
    public boolean isEnable() {
        return this == ENABLE;
    }

    @JsonCreator // 指定反序列化方法
    public static StatusEnum fromCode(Integer code) {
        return getByCode(code); // 复用已有的根据code找枚举的逻辑
    }

    // 告诉Jackson：后端返回枚举时，序列化显示code值（而非枚举名）
    @JsonValue // 指定序列化值
    public Integer getCode() {
        return this.code;
    }

    /**
     * 判断是否为禁用状态
     */
    public boolean isDisable() {
        return this == DISABLE;
    }

    @Override
    public Integer getValue() {
        return this.code;
    }
}
