package com.zesheng.common.enums;

import lombok.Getter;

import java.io.Serializable;

/**
 * 全局返回码枚举
 * 规范：
 * 1. 成功：200
 * 2. 客户端异常（参数/权限）：400xx（参数）、401xx（登录）、403xx（权限）
 * 3. 系统异常：500xx
 * 4. 自定义业务码：按模块分段（10000=用户模块，20000=配置模块）
 */
@Getter
public enum ResultCodeEnum implements Serializable {
    // ========== 通用状态码 ==========

    SUCCESS(200, "操作成功"),
    // ========== 参数相关（400xx） ==========

    PARAM_ERROR(40000, "参数错误"),
    PARAM_EMPTY(40001, "参数不能为空"),
    PARAM_FORMAT_ERROR(40002, "参数格式错误"),
    DUPLICATE_KEY(40003, "数据已存在"),
    // ========== 权限相关 ==========

    UNAUTHORIZED(401, "未登录或token过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "请求接口不存在"),
    // ========== 用户/登录模块（10000段） ==========

    USER_NOT_EXIST(10001, "用户不存在"),
    USER_PAYMENT_INFO_NOT_FOUND(10010, "用户收款信息不存在"),
    USER_PAYMENT_INFO_EXIST(10011, "该用户已填写收款信息"),
    PASSWORD_ERROR(10002, "密码错误"),
    USER_DISABLED(10003, "账号已禁用"),
    TOKEN_EXPIRED(10004, "token已过期"),
    TOKEN_INVALID(10005, "token无效"),

    // ========== 配置模块（20000段） ==========

    CONFIG_NOT_FOUND(20001, "配置项不存在"),
    CONFIG_EXIST(20002, "配置项已存在"),
    // ========== 系统异常（500xx） ==========

    FAIL(50000, "操作失败"),
    DB_ERROR(50001, "数据库操作异常"),
    THIRD_PARTY_ERROR(50002, "第三方接口调用异常");

    private final Integer code;
    private final String msg;

    ResultCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    // 序列化版本号，避免反序列化兼容性问题
    private static final long serialVersionUID = 1L;

    /**
     * 核心工具方法：根据返回码获取枚举值
     *
     * @param code 返回码
     * @return 对应的枚举值，无匹配时返回null（也可抛异常，根据业务选择）
     */
    public static ResultCodeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ResultCodeEnum resultCode : values()) {
            if (resultCode.getCode().equals(code)) {
                return resultCode;
            }
        }
        return null; // 或抛异常：throw new IllegalArgumentException("无效的返回码：" + code);
    }
}