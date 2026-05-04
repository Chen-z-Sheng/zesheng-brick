package com.zesheng.client.service;

import com.zesheng.client.model.request.UpdateUserInfoRequest;
import com.zesheng.client.model.response.WxLoginResponse;
import com.zesheng.common.response.R;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 小程序端认证服务
 */
public interface IAuthService {

    R<WxLoginResponse> wxLogin(String code);

    R<Map<String, String>> getWxOpenId(String code);

    R<WxLoginResponse> wxPhoneQuickLogin(String loginCode, String phoneCode);

    R<WxLoginResponse> donutIdentityLogin(String code);

    R<Void> sendSmsCode(String phone);

    R<WxLoginResponse> smsCodeLogin(String phone, String verifyCode);

    R<WxLoginResponse.UserInfo> getCurrentUser(Long userId);

    R<WxLoginResponse.UserInfo> updateUserInfo(Long userId, UpdateUserInfoRequest request);

    R<com.zesheng.client.controller.AuthController.UploadAvatarResponse> uploadAvatar(Long userId, MultipartFile file);

    R<WxLoginResponse> bindPhone(String code, String openid);

    R<Void> logout(String token);

    R<Map<String, String>> refreshToken(String refreshToken);
}
