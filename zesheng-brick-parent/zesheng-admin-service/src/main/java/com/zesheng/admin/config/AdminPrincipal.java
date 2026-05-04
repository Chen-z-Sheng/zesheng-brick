package com.zesheng.admin.config;

import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Principal;

/**
 * 管理端认证主体，用于在 @PreAuthorize 中访问当前用户ID
 */
@Getter
public class AdminPrincipal implements Principal {

    private final Long userId;
    private final String username;

    public AdminPrincipal(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    @Override
    public String getName() {
        return username;
    }

    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AdminPrincipal principal) {
            return principal.getUserId();
        }
        return null;
    }
}
