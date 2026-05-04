package com.zesheng.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 权限拦截器
 * 验证用户是否有权限访问资源
 */
public class PermissionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求中获取用户信息（例如从ThreadLocal中获取）
        // 这里简化处理，实际应该从认证拦截器中传递的用户信息获取
        
        // 获取请求路径
        String requestUri = request.getRequestURI();
        
        // 简单示例：权限验证逻辑
        // 实际应用中应该根据用户角色和权限表进行验证
        if (requestUri.contains("/admin/") && !hasAdminPermission()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"code\": 403, \"message\": \"权限不足\"}");
            return false;
        }
        
        return true;
    }
    
    /**
     * 检查是否有管理员权限
     * 实际应用中应该从用户信息中获取权限
     */
    private boolean hasAdminPermission() {
        // 这里简化处理，实际应该从用户信息中获取
        return true;
    }
}
