package com.zesheng.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 请求日志：耗时统计使用 request 属性，避免单例拦截器共享字段导致并发串台
 */
@Slf4j
@Component
public class LogInterceptor implements HandlerInterceptor {

    private static final String ATTR_REQUEST_START_MS = "zesheng.request.startMs";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(ATTR_REQUEST_START_MS, System.currentTimeMillis());
        if (log.isDebugEnabled()) {
            log.debug("[请求] {} {} query={} ip={} ua={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    request.getQueryString(),
                    request.getRemoteAddr(),
                    request.getHeader("User-Agent"));
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // 预留：如需组装视图层日志可在此扩展
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Long startMs = (Long) request.getAttribute(ATTR_REQUEST_START_MS);
        long costMs = startMs != null ? System.currentTimeMillis() - startMs : -1L;
        if (ex != null) {
            log.warn("[请求异常] {} {} status={} costMs={}", request.getMethod(), request.getRequestURI(),
                    response.getStatus(), costMs, ex);
        } else {
            log.info("[请求完成] {} {} status={} costMs={}", request.getMethod(), request.getRequestURI(),
                    response.getStatus(), costMs);
        }
    }
}
