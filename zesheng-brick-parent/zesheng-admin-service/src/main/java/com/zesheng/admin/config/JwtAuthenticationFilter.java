package com.zesheng.admin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zesheng.common.enums.ResultCodeEnum;
import com.zesheng.common.response.R;
import com.zesheng.common.util.JwtUtil;
import com.zesheng.sys.model.response.PermissionListResponse;
import com.zesheng.sys.service.IPermissionService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT 认证过滤器
 * 免鉴权路径须与 {@link SecurityConfig} 中 permitAll 一致；
 * 匹配规则使用 {@link AntPathRequestMatcher}，与 {@code authorizeHttpRequests} 使用同一套 Servlet 路径语义，避免手写 URI 与白名单不一致。
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final IPermissionService permissionService;
    private final ObjectMapper objectMapper;
    /** 与 SecurityConfig 中 permitAll 路径一一对应，省略则为重复维护、易出现 401 */
    private final RequestMatcher jwtNotRequiredMatcher;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, IPermissionService permissionService,
            ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.permissionService = permissionService;
        this.objectMapper = objectMapper;
        this.jwtNotRequiredMatcher = new OrRequestMatcher(
                new AntPathRequestMatcher("/doc.html"),
                new AntPathRequestMatcher("/webjars/**"),
                new AntPathRequestMatcher("/v3/api-docs/**"),
                new AntPathRequestMatcher("/knife4j/**"),
                new AntPathRequestMatcher("/auth/**"),
                new AntPathRequestMatcher("/pub/recycle-market/**"));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        // 与 context-path 无关的 URI 截取，避免部分环境下 AntPathRequestMatcher 未命中 /auth/**，
        // 导致 refresh 仍校验过期 Bearer、刷新永远失败
        if (isAuthResourcePath(request)) {
            return true;
        }
        return jwtNotRequiredMatcher.matches(request);
    }

    /**
     * 当前请求映射到应用内路径是否为认证相关（与 {@code /auth/**} 语义一致）
     */
    private static boolean isAuthResourcePath(HttpServletRequest request) {
        String path = pathWithinContextPath(request);
        return path.startsWith("/auth/") || "/auth".equals(path);
    }

    private static String pathWithinContextPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }
        if (uri.isEmpty()) {
            return "/";
        }
        return uri.startsWith("/") ? uri : "/" + uri;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);

            try {
                Claims claims = jwtUtil.parseToken(token);

                Long userId = Long.parseLong(claims.get("userId").toString());
                String username = claims.get("username").toString();

                List<GrantedAuthority> authorities = loadUserAuthorities(userId);

                AdminPrincipal principal = new AdminPrincipal(userId, username);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                request.setAttribute("userId", userId);
                request.setAttribute("username", username);

                log.debug("Token验证成功，用户: {}", username);

                filterChain.doFilter(request, response);

            } catch (ExpiredJwtException e) {
                // Filter 内抛异常不会进入 @ControllerAdvice，须直接写 401，前端 axios 才能走 refresh
                log.warn("Access Token 已过期: {}", e.getMessage());
                writeUnauthorizedJson(response, ResultCodeEnum.TOKEN_EXPIRED);
            } catch (Exception e) {
                log.warn("Token 解析失败: {}", e.getMessage());
                writeUnauthorizedJson(response, ResultCodeEnum.TOKEN_INVALID);
            }
        } else {
            log.warn("Token不存在或格式不正确");
            writeUnauthorizedJson(response, ResultCodeEnum.UNAUTHORIZED);
        }
    }

    /**
     * 在过滤器中直接输出 401 JSON，避免抛异常被容器处理成 500，导致前端无法按 401 刷新 token
     */
    private void writeUnauthorizedJson(HttpServletResponse response, ResultCodeEnum resultCode) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(R.error(resultCode)));
    }

    private List<GrantedAuthority> loadUserAuthorities(Long userId) {
        try {
            R<List<PermissionListResponse>> result = permissionService.getPermissionsByUserId(userId);
            if (result == null || result.getData() == null || !ResultCodeEnum.SUCCESS.getCode().equals(result.getCode())) {
                return Collections.emptyList();
            }
            return result.getData().stream()
                    .filter(p -> p != null && p.getCode() != null && !p.getCode().isBlank())
                    .map(p -> (GrantedAuthority) new SimpleGrantedAuthority(p.getCode()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("加载用户权限失败, userId={}, error={}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }
}
