package com.zesheng.common.config;

import com.zesheng.common.enums.ResultCodeEnum;
import com.zesheng.common.exception.AuthException;
import com.zesheng.common.exception.BizException;
import com.zesheng.common.response.R;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.security.access.AccessDeniedException;
import io.jsonwebtoken.JwtException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public R<String> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("请求接口不存在: {}", e.getRequestURL());
        return R.error(ResultCodeEnum.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<String> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("参数校验失败: {}", e.getMessage());
        return R.error(ResultCodeEnum.PARAM_ERROR.getCode(), e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<String> handleIllegalStateException(IllegalStateException e) {
        log.warn("状态异常: {}", e.getMessage());
        return R.error(ResultCodeEnum.PARAM_ERROR.getCode(), e.getMessage());
    }

    @ExceptionHandler(MissingPathVariableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<String> handleMissingPathVariableException(MissingPathVariableException e) {
        log.warn("缺少路径参数: {}", e.getVariableName());
        return R.error(ResultCodeEnum.PARAM_ERROR.getCode(), "缺少路径参数: " + e.getVariableName());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<String> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.warn("缺少请求参数: {}", e.getParameterName());
        return R.error(ResultCodeEnum.PARAM_ERROR.getCode(), "缺少请求参数: " + e.getParameterName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<String> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型不匹配: {}", e.getName());
        return R.error(ResultCodeEnum.PARAM_ERROR.getCode(), "参数类型不匹配: " + e.getName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("请求体格式错误: {}", e.getMessage());
        return R.error(ResultCodeEnum.PARAM_ERROR.getCode(), "请求体格式错误，请检查JSON格式");
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<String> handleHandlerMethodValidationException(HandlerMethodValidationException e) {
        String msg = e.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = ResultCodeEnum.PARAM_ERROR.getMsg();
        }
        log.warn("Web 方法参数校验失败: {}", msg);
        return R.error(ResultCodeEnum.PARAM_ERROR.getCode(), msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<String> handleConstraintViolationException(ConstraintViolationException e) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("路径/查询参数校验失败: {}", errorMessage);
        return R.error(ResultCodeEnum.PARAM_ERROR.getCode(), errorMessage);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> {
                    String defaultMessage = fieldError.getDefaultMessage();
                    String fieldName = fieldError.getField();
                    Class<?> targetClass = e.getTarget().getClass();
                    
                    String chineseFieldName = getChineseFieldName(targetClass, fieldName);
                    
                    String replacedMessage;
                    if (defaultMessage.contains("{0}")) {
                        replacedMessage = defaultMessage.replace("{0}", chineseFieldName);
                    } else {
                        replacedMessage = chineseFieldName + defaultMessage;
                    }
                    
                    return replacedMessage;
                })
                .collect(Collectors.joining("; "));
        log.warn("参数验证失败: {}", errorMessage);
        return R.error(ResultCodeEnum.PARAM_ERROR.getCode(), errorMessage);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<String> handleDuplicateKeyException(DuplicateKeyException e) {
        String errorMessage = extractDuplicateKeyMessage(e.getMessage());
        log.warn("数据重复: {}", errorMessage);
        return R.error(ResultCodeEnum.DUPLICATE_KEY.getCode(), errorMessage);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<String> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        String errorMessage = extractDuplicateKeyMessage(e.getMessage());
        log.warn("数据完整性约束违反: {}", errorMessage);
        return R.error(ResultCodeEnum.DUPLICATE_KEY.getCode(), errorMessage);
    }

    private String extractDuplicateKeyMessage(String exceptionMessage) {
        if (exceptionMessage == null) {
            return "数据已存在";
        }
        
        String message = exceptionMessage;
        
        Pattern pattern = Pattern.compile("Duplicate entry '(.+?)' for key '(.+?)'", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(message);
        
        if (matcher.find()) {
            String value = matcher.group(1);
            String key = matcher.group(2);
            
            if (key.contains("username")) {
                return "用户名 '" + value + "' 已存在";
            } else if (key.contains("email")) {
                return "邮箱 '" + value + "' 已存在";
            } else if (key.contains("phone") || key.contains("mobile")) {
                return "手机号 '" + value + "' 已存在";
            } else if (key.contains("name")) {
                return "名称 '" + value + "' 已存在";
            } else if (key.contains("code")) {
                return "编码 '" + value + "' 已存在";
            } else {
                return "数据 '" + value + "' 已存在";
            }
        }
        
        pattern = Pattern.compile("唯一索引|唯一约束|unique constraint|unique index", Pattern.CASE_INSENSITIVE);
        if (pattern.matcher(message).find()) {
            return "数据已存在，请检查是否重复";
        }
        
        return "数据已存在";
    }

    private String getChineseFieldName(Class<?> clazz, String fieldName) {
        try {
            Field field = findField(clazz, fieldName);
            if (field != null) {
                Schema schema = field.getAnnotation(Schema.class);
                if (schema != null) {
                    String name = schema.name();
                    String description = schema.description();
                    if (name != null && !name.isEmpty()) {
                        return name;
                    }
                    if (description != null && !description.isEmpty()) {
                        return description;
                    }
                }
            }
            return fieldName;
        } catch (Exception e) {
            return fieldName;
        }
    }

    private Field findField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null) {
                return findField(superClass, fieldName);
            }
            return null;
        }
    }

    @ExceptionHandler(BizException.class)
    public ResponseEntity<R<String>> handleBizException(BizException e) {
        HttpStatus status = resolveBizHttpStatus(e.getCode());
        log.warn("业务异常: [{}] {}", e.getCode(), e.getMessage());
        return ResponseEntity.status(status).body(R.error(e.getCode(), e.getMessage()));
    }

    private static HttpStatus resolveBizHttpStatus(Integer code) {
        if (code == null) {
            return HttpStatus.BAD_REQUEST;
        }
        if (code.equals(ResultCodeEnum.UNAUTHORIZED.getCode())) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (code.equals(ResultCodeEnum.FORBIDDEN.getCode())) {
            return HttpStatus.FORBIDDEN;
        }
        if (code.equals(ResultCodeEnum.NOT_FOUND.getCode())) {
            return HttpStatus.NOT_FOUND;
        }
        if (code >= 50000) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.BAD_REQUEST;
    }

    @ExceptionHandler(AuthException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R<String> handleAuthException(AuthException e) {
        log.warn("认证异常: {}", e.getMessage());
        return R.error(e.getCode(), e.getMessage());
    }

    /** JWT 解析失败（过期、签名无效、格式错误等）统一返回 401，便于前端走刷新 token 或重新登录 */
    @ExceptionHandler(JwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R<String> handleJwtException(JwtException e) {
        log.warn("JWT 校验失败: {}", e.getMessage());
        return R.error(ResultCodeEnum.UNAUTHORIZED.getCode(), ResultCodeEnum.UNAUTHORIZED.getMsg());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R<String> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return R.error(ResultCodeEnum.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<String> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return R.error(ResultCodeEnum.FAIL);
    }
}
