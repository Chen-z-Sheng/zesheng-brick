package com.zesheng.client.config;

import com.zesheng.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 * 验证请求中的 JWT token 并设置认证信息
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        String pathWithinApp = (contextPath != null && path.startsWith(contextPath))
                ? path.substring(contextPath.length()) : path;
        return pathWithinApp.startsWith("/banner/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 放行OPTIONS预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        // 从请求头中获取 token
        String token = request.getHeader("Authorization");
        
        // 验证 token 是否存在且格式正确
        if (token != null && token.startsWith("Bearer ")) {
            // 移除 Bearer 前缀
            token = token.substring(7);
            
            try {
                // 解析 token
                Claims claims = jwtUtil.parseToken(token);
                
                // 从 token 中获取用户信息
                Long userId = Long.parseLong(claims.get("userId").toString());
                String username = claims.get("username").toString();
                
                // 创建认证令牌
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username, null, null
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // 设置认证信息到安全上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                // 将用户信息存储到请求属性中，方便后续使用
                request.setAttribute("userId", userId);
                request.setAttribute("username", username);
                
            } catch (Exception e) {
                // token 解析失败，清除认证信息
                SecurityContextHolder.clearContext();
            }
        }
        
        // 继续执行过滤链
        filterChain.doFilter(request, response);
    }
}