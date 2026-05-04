package com.zesheng.common.exception;

import com.zesheng.common.enums.ResultCodeEnum;
import lombok.Getter;

/**
 * 统一业务异常，Service 层抛出；由全局异常处理器转换为 R 响应
 */
@Getter
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Integer code;

    public BizException(ResultCodeEnum resultCode) {
        super(resultCode != null ? resultCode.getMsg() : ResultCodeEnum.FAIL.getMsg());
        this.code = resultCode != null ? resultCode.getCode() : ResultCodeEnum.FAIL.getCode();
    }

    public BizException(ResultCodeEnum resultCode, String message) {
        super(message);
        this.code = resultCode != null ? resultCode.getCode() : ResultCodeEnum.FAIL.getCode();
    }

    public BizException(ResultCodeEnum resultCode, String message, Throwable cause) {
        super(message, cause);
        this.code = resultCode != null ? resultCode.getCode() : ResultCodeEnum.FAIL.getCode();
    }

    public BizException(Integer code, String message) {
        super(message);
        this.code = code != null ? code : ResultCodeEnum.FAIL.getCode();
    }
}
