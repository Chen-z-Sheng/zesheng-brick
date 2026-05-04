package com.zesheng.admin.service;

import com.zesheng.admin.model.request.LoginRequest;
import com.zesheng.admin.model.response.LoginResponse;
import com.zesheng.common.response.R;
import jakarta.servlet.http.HttpServletRequest;

public interface IAuthService {
    R<LoginResponse> login(LoginRequest loginRequest, HttpServletRequest httpRequest);
    
    /**
     * 刷新访问令牌
     * @param refreshToken 刷新令牌
     * @return 新的登录响应，包含新的访问令牌和刷新令牌
     */
    R<LoginResponse> refreshToken(String refreshToken);

    /**
     * 退出登录，使当前访问令牌失效
     * @param accessToken 当前访问令牌（从 Authorization 头获取）
     * @return 统一响应
     */
    R<Void> logout(String accessToken);
}
