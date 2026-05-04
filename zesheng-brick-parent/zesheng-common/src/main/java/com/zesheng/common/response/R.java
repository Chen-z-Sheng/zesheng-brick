package com.zesheng.common.response;

import org.springframework.http.HttpStatus;
import com.zesheng.common.enums.ResultCodeEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class R<T> implements Serializable {

    private Integer code;
    private String msg;
    private T data;

    private R() {
    }

    private R(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // 无参success方法
    public static <T> R<T> success() {
        return new R<>(ResultCodeEnum.SUCCESS.getCode(), ResultCodeEnum.SUCCESS.getMsg(), null);
    }

    // 结合枚举的静态方法
    public static <T> R<T> success(T data) {
        return new R<>(ResultCodeEnum.SUCCESS.getCode(), ResultCodeEnum.SUCCESS.getMsg(), data);
    }

    public static <T> R<T> success(String msg, T data) {
        return new R<>(ResultCodeEnum.SUCCESS.getCode(), msg, data);
    }

    public static <T> R<T> success(HttpStatus status, T data) {
        return new R<>(status.value(), status.getReasonPhrase(), data);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(ResultCodeEnum.SUCCESS.getCode(), ResultCodeEnum.SUCCESS.getMsg(), data);
    }

    /**
     * 失败返回（用枚举）
     */
    public static <T> R<T> error(ResultCodeEnum resultCodeEnum) {
        return new R<>(resultCodeEnum.getCode(), resultCodeEnum.getMsg(), null);
    }

    public static <T> R<T> error(ResultCodeEnum resultCodeEnum, T data) {
        return new R<>(resultCodeEnum.getCode(), resultCodeEnum.getMsg(), data);
    }

    // 通用方法
    public static <T> R<T> error(Integer code, String msg) {
        return new R<>(code, msg, null);
    }

    public static <T> R<T> error(String msg) {
        return new R<>(ResultCodeEnum.FAIL.getCode(), msg, null);
    }
}