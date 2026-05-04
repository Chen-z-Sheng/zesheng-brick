package com.zesheng.admin.config;

import com.zesheng.common.constant.InternalApiHeaders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 管理端 /pub/recycle-market：仅允许携带正确服务间密钥的 C 端调用（非管理端用户 JWT）
 */
@Component
@Slf4j
public class PubRecycleMarketInternalApiKeyFilter extends OncePerRequestFilter {

    private final RequestMatcher pathMatcher = new AntPathRequestMatcher("/pub/recycle-market/**");

    @Value("${zesheng.internal.recycle-market-api-key:}")
    private String expectedKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!pathMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!StringUtils.hasText(expectedKey)) {
            log.error("未配置 zesheng.internal.recycle-market-api-key，拒绝 /pub/recycle-market 访问");
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":503,\"msg\":\"服务间密钥未配置\"}");
            return;
        }
        String provided = request.getHeader(InternalApiHeaders.RECYCLE_MARKET_INTERNAL_KEY);
        if (!expectedKey.equals(provided)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"服务间调用凭证无效\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
