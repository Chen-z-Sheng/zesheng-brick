package com.zesheng.common.exception;

import com.zesheng.common.enums.ResultCodeEnum;

/**
 * 认证异常类
 * 用于处理JWT认证过程中的异常
 */
public class AuthException extends RuntimeException {
    
    private int code;
    
    public AuthException(String message) {
        super(message);
        this.code = ResultCodeEnum.UNAUTHORIZED.getCode();
    }
    
    public AuthException(String message, int code) {
        super(message);
        this.code = code;
    }
    
    public AuthException(ResultCodeEnum resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
    }
    
    public int getCode() {
        return code;
    }
}
