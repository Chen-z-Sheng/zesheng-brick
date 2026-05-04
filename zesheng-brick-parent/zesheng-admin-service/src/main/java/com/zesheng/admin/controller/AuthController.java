package com.zesheng.admin.controller;

import com.zesheng.admin.model.request.LoginRequest;
import com.zesheng.admin.model.request.RefreshTokenRequest;
import com.zesheng.admin.model.response.LoginResponse;
import com.zesheng.admin.service.IAuthService;
import com.zesheng.common.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 * 处理用户登录和令牌刷新等认证相关操作
 */
@RestController
@RequestMapping("auth")
@Tag(name = "管理端-登录模块", description = "管理端-用户端登录相关接口")
@RequiredArgsConstructor
public class AuthController {
    private final IAuthService authService;

    @PostMapping("login")
    @Operation(summary = "用户登录", description = "用户名密码登录，返回JWT token")
    public R<LoginResponse> login(
            @Validated @RequestBody LoginRequest loginRequest,
            HttpServletRequest httpRequest) {
        return authService.login(loginRequest, httpRequest);
    }

    @PostMapping("refresh")
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    public R<LoginResponse> refresh(@Validated @RequestBody RefreshTokenRequest refreshTokenRequest) {
        return authService.refreshToken(refreshTokenRequest.getRefreshToken());
    }

    @PostMapping("logout")
    @Operation(summary = "退出登录", description = "使当前访问令牌失效")
    public R<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        return authService.logout(token);
    }
}
