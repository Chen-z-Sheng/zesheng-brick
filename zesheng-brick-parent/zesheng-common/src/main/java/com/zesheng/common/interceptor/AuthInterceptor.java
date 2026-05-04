package com.zesheng.common.interceptor;

import com.zesheng.common.enums.ResultCodeEnum;
import com.zesheng.common.util.JwtUtil;
import com.zesheng.common.util.RedisUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 认证拦截器
 * 验证用户是否登录，实现四层Token校验
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 CORS 预检请求（浏览器不会对 OPTIONS 携带 Authorization，必须直接放行）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 从请求头获取Token
        String authHeader = request.getHeader("Authorization");
        
        // 第一步：校验Token格式
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return writeErrorResponse(response, ResultCodeEnum.UNAUTHORIZED);
        }
        
        String token = authHeader.substring(7);
        Claims claims;
        
        try {
            // 第二步：校验Token签名是否合法、是否被篡改
            claims = jwtUtil.parseToken(token);
        } catch (ExpiredJwtException e) {
            // Token已过期，返回需要刷新的状态码
            return writeErrorResponse(response, ResultCodeEnum.TOKEN_EXPIRED);
        } catch (Exception e) {
            // Token无效（签名错误、格式错误等）
            log.warn("Token解析失败：{}", e.getMessage());
            return writeErrorResponse(response, ResultCodeEnum.TOKEN_INVALID);
        }
        
        // 第三步：校验Token是否在Redis中存在（是否已被注销）
        String redisKey = "user:token:map:" + token;
        if (!redisUtil.hasKey(redisKey)) {
            log.warn("Token已失效或已被注销：{}", token);
            return writeErrorResponse(response, ResultCodeEnum.TOKEN_INVALID);
        }
        
        // 第四步：校验Token用户与Redis存储的用户是否匹配
        Long jwtUserId = claims.get("userId", Long.class);
        Object redisValue = redisUtil.get(redisKey);
        Long redisUserId = null;
        if (redisValue != null) {
            if (redisValue instanceof Integer) {
                redisUserId = ((Integer) redisValue).longValue();
            } else if (redisValue instanceof Long) {
                redisUserId = (Long) redisValue;
            } else if (redisValue instanceof String) {
                try {
                    redisUserId = Long.parseLong((String) redisValue);
                } catch (NumberFormatException e) {
                    log.warn("Redis中存储的userId格式不正确：{}", redisValue);
                    return writeErrorResponse(response, ResultCodeEnum.TOKEN_INVALID);
                }
            }
        }
        if (jwtUserId == null || redisUserId == null || !jwtUserId.equals(redisUserId)) {
            log.warn("Token用户不匹配，JWT userId：{}，Redis userId：{}", jwtUserId, redisUserId);
            return writeErrorResponse(response, ResultCodeEnum.TOKEN_INVALID);
        }
        
        // 校验通过，将用户ID存入request，方便后续业务使用
        request.setAttribute("userId", jwtUserId);
        request.setAttribute("username", claims.get("username", String.class));
        
        return true;
    }

    /**
     * 输出错误响应
     */
    private boolean writeErrorResponse(HttpServletResponse response, ResultCodeEnum resultCode) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\": " + resultCode.getCode() + ", \"msg\": \"" + resultCode.getMsg() + "\"}");
        return false;
    }
}
