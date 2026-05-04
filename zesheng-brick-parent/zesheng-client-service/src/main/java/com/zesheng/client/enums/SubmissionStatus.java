package com.zesheng.client.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 提交状态枚举
 * 包含状态码和对应的描述，统一管理提交相关的所有状态
 */
@Getter
public enum SubmissionStatus {
    DRAFT(0, "草稿"),          // 草稿

    SUBMITTED(1, "已提交"),     // 已提交

    TRANSIT(2, "运输中"),       // 运输中

    RECEIVED(7, "已签收"),      // 已签收（快递签收后，待验货入库）

    STORING(3, "入库中"),       // 入库中

    PAID(4, "已打款"),          // 已打款

    EXCEPTION(5, "异常"),       // 异常

    RETURNED(6, "已退货");      // 已退货


    private static final long serialVersionUID = 1L;

    /**
     * 状态码（数字标识，用于存储/传输），与数据库 status 列对应；API 序列化也使用 code
     */
    @EnumValue
    @JsonValue
    private final Integer code;

    /**
     * 状态描述（文字说明，用于展示）
     */
    private final String desc;

    SubmissionStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static SubmissionStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SubmissionStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        // 找不到时
        throw new IllegalArgumentException("无效的提交状态码：" + code);
    }
}
