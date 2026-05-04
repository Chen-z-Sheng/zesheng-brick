package com.zesheng.client.controller;

import com.zesheng.client.model.request.BindPhoneRequest;
import com.zesheng.client.model.request.DonutLoginRequest;
import com.zesheng.client.model.request.RefreshTokenRequest;
import com.zesheng.client.model.request.SmsCodeLoginRequest;
import com.zesheng.client.model.request.SmsCodeSendRequest;
import com.zesheng.client.model.request.UpdateUserInfoRequest;
import com.zesheng.client.model.request.WxLoginRequest;
import com.zesheng.client.model.request.WxPhoneQuickLoginRequest;
import com.zesheng.client.model.response.WxLoginResponse;
import com.zesheng.client.service.IAuthService;
import com.zesheng.common.enums.ResultCodeEnum;
import com.zesheng.common.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("auth")
@Tag(name = "小程序-认证", description = "登录与用户信息")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final IAuthService authService;

    @PostMapping("wx-login")
    @Operation(summary = "微信登录")
    public R<WxLoginResponse> wxLogin(@Validated @RequestBody WxLoginRequest request) {
        return authService.wxLogin(request.getCode());
    }

    @PostMapping("wx-openid")
    @Operation(summary = "通过code换取openId")
    public R<Map<String, String>> wxOpenId(@Validated @RequestBody WxLoginRequest request) {
        return authService.getWxOpenId(request.getCode());
    }

    @PostMapping("wx-phone-login")
    @Operation(summary = "手机号快捷登录")
    public R<WxLoginResponse> wxPhoneQuickLogin(@Validated @RequestBody WxPhoneQuickLoginRequest request) {
        return authService.wxPhoneQuickLogin(request.getLoginCode(), request.getPhoneCode());
    }

    @PostMapping("donut-login")
    @Operation(summary = "多端身份登录")
    public R<WxLoginResponse> donutLogin(@Validated @RequestBody DonutLoginRequest request) {
        return authService.donutIdentityLogin(request.getCode());
    }

    @PostMapping("send-sms-code")
    @Operation(summary = "发送短信验证码")
    public R<Void> sendSmsCode(@Validated @RequestBody SmsCodeSendRequest request) {
        return authService.sendSmsCode(request.getPhone());
    }

    @PostMapping("sms-login")
    @Operation(summary = "短信验证码登录")
    public R<WxLoginResponse> smsLogin(@Validated @RequestBody SmsCodeLoginRequest request) {
        return authService.smsCodeLogin(request.getPhone(), request.getVerifyCode());
    }

    @GetMapping("current-user")
    @Operation(summary = "获取当前用户")
    public R<WxLoginResponse.UserInfo> getCurrentUser(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return R.error(ResultCodeEnum.UNAUTHORIZED);
        }
        return authService.getCurrentUser(userId);
    }

    @PostMapping("update-user-info")
    @Operation(summary = "更新用户信息")
    public R<WxLoginResponse.UserInfo> updateUserInfo(
            @Validated @RequestBody UpdateUserInfoRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        if (userId == null) {
            return R.error(ResultCodeEnum.UNAUTHORIZED);
        }
        return authService.updateUserInfo(userId, request);
    }

    @PostMapping("upload-avatar")
    @Operation(summary = "上传头像")
    public R<UploadAvatarResponse> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return R.error(ResultCodeEnum.UNAUTHORIZED);
        }
        return authService.uploadAvatar(userId, file);
    }

    @PostMapping("bind-phone")
    @Operation(summary = "绑定手机号")
    public R<WxLoginResponse> bindPhone(@Validated @RequestBody BindPhoneRequest request) {
        return authService.bindPhone(request.getCode(), request.getOpenid());
    }

    @PostMapping("logout")
    @Operation(summary = "退出登录")
    public R<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        return authService.logout(token);
    }

    @PostMapping("refresh-token")
    @Operation(summary = "刷新令牌")
    public R<Map<String, String>> refreshToken(@Validated @RequestBody RefreshTokenRequest request) {
        return authService.refreshToken(request.getRefreshToken());
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId instanceof Long) {
            return (Long) userId;
        }
        return null;
    }

    @lombok.Data
    public static class UploadAvatarResponse {
        private String url;
    }
}
