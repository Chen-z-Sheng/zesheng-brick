package com.zesheng.client.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.zesheng.client.controller.AuthController;
import com.zesheng.client.entity.User;
import com.zesheng.client.mapper.UserMapper;
import com.zesheng.client.model.request.UpdateUserInfoRequest;
import com.zesheng.client.model.response.WxLoginResponse;
import com.zesheng.client.service.IAuthService;
import com.zesheng.client.service.IOssService;
import com.zesheng.common.enums.StatusEnum;
import com.zesheng.common.response.R;
import com.zesheng.common.util.JwtUtil;
import com.zesheng.common.util.RedisUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.concurrent.ThreadLocalRandom;
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final IOssService ossService;
    private final RestTemplate restTemplate;
    private final ObjectMapper jsonMapper;

    @Value("${wx.miniapp.appid:}")
    private String wxAppId;

    @Value("${wx.miniapp.secret:}")
    private String wxSecret;

    @Value("${wx.donut.appid:}")
    private String donutAppId;

    @Value("${wx.donut.appsecret:}")
    private String donutAppSecret;

    @Value("${aliyun.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${aliyun.sms.access-key-id:}")
    private String smsAccessKeyId;

    @Value("${aliyun.sms.access-key-secret:}")
    private String smsAccessKeySecret;

    @Value("${aliyun.sms.endpoint:dysmsapi.aliyuncs.com}")
    private String smsEndpoint;

    @Value("${aliyun.sms.sign-name:}")
    private String smsSignName;

    @Value("${aliyun.sms.template-code:}")
    private String smsTemplateCode;

    @Value("${aliyun.sms.code-expire-seconds:300}")
    private long smsCodeExpireSeconds;

    @Value("${aliyun.sms.send-interval-seconds:60}")
    private long smsSendIntervalSeconds;

    // 微信登录凭证校验接口
    private static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";
    // 获取手机号接口
    private static final String WX_PHONE_URL = "https://api.weixin.qq.com/wxa/business/getuserphonenumber";
    // 获取access_token接口
    private static final String WX_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";

    // Token过期时间（7天）
    private static final long TOKEN_EXPIRE_DAYS = 7;
    // Token过期时间（2小时）
    private static final long TOKEN_EXPIRE_HOURS = 2;
    // 微信默认灰色头像
    private static final String DEFAULT_AVATAR = "https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0";
    private static final String SMS_CODE_KEY_PREFIX = "client:sms:code:";
    private static final String SMS_INTERVAL_KEY_PREFIX = "client:sms:interval:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<WxLoginResponse> wxLogin(String code) {
        try {
            Map<String, String> wxSession = getWxSession(code);
            if (wxSession == null || wxSession.get("openid") == null) {
                return R.error("微信登录失败，请重试");
            }

            String openid = wxSession.get("openid");
            String sessionKey = wxSession.get("session_key");

            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getOpenid, openid);
            User user = userMapper.selectOne(wrapper);

            boolean isNewUser = false;
            if (user == null) {
                user = new User();
                user.setOpenid(openid);
                user.setSessionKey(sessionKey);
                user.setStatus(StatusEnum.ENABLE);
                userMapper.insert(user);
                applyDefaultNickAndAvatar(user);
                isNewUser = true;
                log.info("新用户注册成功，userId: {}", user.getId());
            } else {
                user.setSessionKey(sessionKey);
                userMapper.updateById(user);
                log.info("老用户登录成功，userId: {}", user.getId());
            }

            return R.ok(issueTokensAndBuildResponse(user, isNewUser));
        } catch (Exception e) {
            log.error("微信登录异常", e);
            return R.error("登录失败，请重试");
        }
    }

    @Override
    public R<Map<String, String>> getWxOpenId(String code) {
        try {
            Map<String, String> wxSession = getWxSession(code);
            if (wxSession == null || !StringUtils.hasText(wxSession.get("openid"))) {
                return R.error("获取openId失败，请重试");
            }

            Map<String, String> result = new HashMap<>(1);
            result.put("openId", wxSession.get("openid"));
            return R.ok(result);
        } catch (Exception e) {
            log.error("通过code获取openId异常", e);
            return R.error("获取openId失败，请重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<WxLoginResponse> wxPhoneQuickLogin(String loginCode, String phoneCode) {
        try {
            Map<String, String> wxSession = getWxSession(loginCode);
            if (wxSession == null || wxSession.get("openid") == null) {
                return R.error("微信登录失败，请重试");
            }
            String openid = wxSession.get("openid");
            String sessionKey = wxSession.get("session_key");

            String accessToken = getWxAccessToken();
            if (accessToken == null) {
                return R.error("获取微信凭证失败");
            }
            String phone = getWxPhoneNumber(accessToken, phoneCode);
            if (!StringUtils.hasText(phone)) {
                return R.error("获取手机号失败");
            }

            LambdaQueryWrapper<User> byOpenid = new LambdaQueryWrapper<>();
            byOpenid.eq(User::getOpenid, openid);
            User openidUser = userMapper.selectOne(byOpenid);

            LambdaQueryWrapper<User> byPhone = new LambdaQueryWrapper<>();
            byPhone.eq(User::getPhone, phone);
            User phoneUser = userMapper.selectOne(byPhone);

            if (openidUser != null) {
                if (phoneUser != null && !phoneUser.getId().equals(openidUser.getId())) {
                    return R.error("该手机号已绑定其他账号");
                }
                openidUser.setSessionKey(sessionKey);
                openidUser.setPhone(phone);
                userMapper.updateById(openidUser);
                log.info("手机号快捷登录成功（openid 已存在），userId: {}", openidUser.getId());
                return R.ok(issueTokensAndBuildResponse(openidUser, false));
            }

            if (phoneUser != null) {
                if (StringUtils.hasText(phoneUser.getOpenid()) && !openid.equals(phoneUser.getOpenid())) {
                    return R.error("该手机号已绑定其他微信账号");
                }
                phoneUser.setOpenid(openid);
                phoneUser.setSessionKey(sessionKey);
                userMapper.updateById(phoneUser);
                log.info("手机号快捷登录成功（合并已有手机号账号），userId: {}", phoneUser.getId());
                return R.ok(issueTokensAndBuildResponse(phoneUser, false));
            }

            User user = new User();
            user.setOpenid(openid);
            user.setSessionKey(sessionKey);
            user.setPhone(phone);
            user.setStatus(StatusEnum.ENABLE);
            userMapper.insert(user);
            applyDefaultNickAndAvatar(user);
            log.info("手机号快捷登录新用户注册，userId: {}", user.getId());
            return R.ok(issueTokensAndBuildResponse(user, true));
        } catch (Exception e) {
            log.error("手机号快捷登录异常", e);
            return R.error("登录失败，请重试");
        }
    }

    private WxLoginResponse issueTokensAndBuildResponse(User user, boolean isNewUser) {
        String token = jwtUtil.generateToken(user.getId(), user.getNickName());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getNickName());
        String redisKey = "client:token:" + user.getId();
        redisUtil.set(redisKey, token, TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);
        String refreshTokenKey = "client:refreshToken:" + user.getId();
        redisUtil.set(refreshTokenKey, refreshToken, 7, TimeUnit.DAYS);
        redisUtil.set("user:token:map:" + token, user.getId(), TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);

        WxLoginResponse response = new WxLoginResponse();
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        response.setRegistered(true);
        response.setNewUser(isNewUser);

        WxLoginResponse.UserInfo userInfo = new WxLoginResponse.UserInfo();
        userInfo.setUserId(user.getId());
        userInfo.setNickName(user.getNickName());
        userInfo.setAvatarUrl(user.getAvatarUrl());
        userInfo.setPhone(user.getPhone());
        response.setUserInfo(userInfo);
        return response;
    }

    private void applyDefaultNickAndAvatar(User user) {
        String userIdStr = String.valueOf(user.getId());
        String suffix = userIdStr.length() >= 4
                ? userIdStr.substring(userIdStr.length() - 4)
                : userIdStr;
        user.setNickName("用户" + suffix);
        user.setAvatarUrl(DEFAULT_AVATAR);
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<WxLoginResponse> donutIdentityLogin(String code) {
        try {
            if (!StringUtils.hasText(code)) {
                return R.error("缺少登录凭证");
            }
            Optional<DonutSessionInfo> sessionOpt = exchangeDonutIdentityCode(code);
            if (sessionOpt.isEmpty()) {
                return R.error("身份校验失败，请检查多端应用配置或稍后重试");
            }
            DonutSessionInfo session = sessionOpt.get();
            if (!StringUtils.hasText(session.phone())) {
                return R.error("未获取到手机号，请重试");
            }
            String phone = session.phone().trim();
            String donutId = StringUtils.hasText(session.donutUserId()) ? session.donutUserId().trim() : "";

            User user = null;
            if (StringUtils.hasText(donutId)) {
                user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getDonutUserId, donutId));
            }
            if (user == null) {
                user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
            }
            if (user != null) {
                if (StringUtils.hasText(donutId) && StringUtils.hasText(user.getDonutUserId())
                        && !donutId.equals(user.getDonutUserId())) {
                    return R.error("该手机号已绑定其他微信身份账号");
                }
                boolean changed = false;
                if (StringUtils.hasText(donutId) && !StringUtils.hasText(user.getDonutUserId())) {
                    user.setDonutUserId(donutId);
                    changed = true;
                }
                if (!StringUtils.hasText(user.getPhone()) || !phone.equals(user.getPhone())) {
                    user.setPhone(phone);
                    changed = true;
                }
                if (changed) {
                    userMapper.updateById(user);
                }
                log.info("多端身份登录成功，userId: {}", user.getId());
                return R.ok(issueTokensAndBuildResponse(user, false));
            }

            User nu = new User();
            nu.setDonutUserId(StringUtils.hasText(donutId) ? donutId : null);
            nu.setPhone(phone);
            nu.setStatus(StatusEnum.ENABLE);
            userMapper.insert(nu);
            applyDefaultNickAndAvatar(nu);
            log.info("多端身份登录新用户注册，userId: {}", nu.getId());
            return R.ok(issueTokensAndBuildResponse(nu, true));
        } catch (Exception e) {
            log.error("多端身份登录异常", e);
            return R.error("登录失败，请重试");
        }
    }

    @Override
    public R<Void> sendSmsCode(String phone) {
        if (!smsEnabled) {
            return R.error("短信登录未开启");
        }
        if (!isSmsConfigured()) {
            log.warn("阿里云短信配置缺失，请检查 accessKey/signName/templateCode");
            return R.error("短信服务配置不完整");
        }
        String intervalKey = SMS_INTERVAL_KEY_PREFIX + phone;
        if (Boolean.TRUE.equals(redisUtil.hasKey(intervalKey))) {
            return R.error("发送过于频繁，请稍后再试");
        }
        String verifyCode = generateSmsCode();
        if (!doSendAliyunSms(phone, verifyCode)) {
            return R.error("验证码发送失败，请稍后重试");
        }
        // 存储验证码并限制重发频率
        redisUtil.set(SMS_CODE_KEY_PREFIX + phone, verifyCode, smsCodeExpireSeconds, TimeUnit.SECONDS);
        redisUtil.set(intervalKey, "1", smsSendIntervalSeconds, TimeUnit.SECONDS);
        return R.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<WxLoginResponse> smsCodeLogin(String phone, String verifyCode) {
        if (!smsEnabled) {
            return R.error("短信登录未开启");
        }
        String cacheKey = SMS_CODE_KEY_PREFIX + phone;
        Object cachedCode = redisUtil.get(cacheKey);
        if (cachedCode == null || !verifyCode.equals(String.valueOf(cachedCode))) {
            return R.error("验证码错误或已过期");
        }
        // 验证通过后立即失效，防止重复使用
        redisUtil.delete(cacheKey);

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (user != null) {
            log.info("短信验证码登录成功，userId: {}", user.getId());
            return R.ok(issueTokensAndBuildResponse(user, false));
        }
        User newUser = new User();
        newUser.setPhone(phone);
        newUser.setStatus(StatusEnum.ENABLE);
        userMapper.insert(newUser);
        applyDefaultNickAndAvatar(newUser);
        log.info("短信验证码登录新用户注册，userId: {}", newUser.getId());
        return R.ok(issueTokensAndBuildResponse(newUser, true));
    }

    private Optional<DonutSessionInfo> exchangeDonutIdentityCode(String code) {
        if (!StringUtils.hasText(donutAppId) || !StringUtils.hasText(donutAppSecret)) {
            log.warn("多端应用未配置：请在配置中设置 wx.donut.appid 与 wx.donut.appsecret");
            return Optional.empty();
        }
        try {
            URI uri = UriComponentsBuilder
                    .fromUriString("https://api.weixin.qq.com/donut/code2verifyinfo")
                    .queryParam("appid", donutAppId)
                    .queryParam("appsecret", donutAppSecret)
                    .queryParam("code", code)
                    .queryParam("grant_type", "authorization_code")
                    .build()
                    .toUri();
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
            String body = response.getBody();
            if (body == null) {
                log.warn("code2verifyinfo 响应体为空");
                return Optional.empty();
            }
            JsonNode root = jsonMapper.readTree(body);
            if (root.has("errcode") && root.get("errcode").asInt() != 0) {
                log.warn("code2verifyinfo 失败 errcode={}, errmsg={}",
                        root.get("errcode").asInt(),
                        root.has("errmsg") ? root.get("errmsg").asText() : "");
                return Optional.empty();
            }
            JsonNode userInfo = root.path("user_info");
            String userId = userInfo.hasNonNull("user_id") ? userInfo.get("user_id").asText() : "";
            String phone = extractPhoneFromDonutResponse(userInfo.path("phone_info"));
            if (!StringUtils.hasText(phone)) {
                log.warn("code2verifyinfo 未返回手机号");
                return Optional.empty();
            }
            return Optional.of(new DonutSessionInfo(userId, phone));
        } catch (Exception e) {
            log.error("调用 code2verifyinfo 异常", e);
            return Optional.empty();
        }
    }

    private String extractPhoneFromDonutResponse(JsonNode phoneInfo) {
        if (phoneInfo == null || phoneInfo.isMissingNode() || phoneInfo.isNull()) {
            return "";
        }
        if (phoneInfo.hasNonNull("phone")) {
            return phoneInfo.get("phone").asText();
        }
        if (phoneInfo.hasNonNull("purePhoneNumber")) {
            return phoneInfo.get("purePhoneNumber").asText();
        }
        if (phoneInfo.hasNonNull("phoneNumber")) {
            return phoneInfo.get("phoneNumber").asText();
        }
        return "";
    }

    private record DonutSessionInfo(String donutUserId, String phone) {
    }

    private boolean isSmsConfigured() {
        return StringUtils.hasText(smsAccessKeyId)
                && StringUtils.hasText(smsAccessKeySecret)
                && StringUtils.hasText(smsSignName)
                && StringUtils.hasText(smsTemplateCode);
    }

    private String generateSmsCode() {
        int code = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return String.valueOf(code);
    }

    private boolean doSendAliyunSms(String phone, String verifyCode) {
        try {
            com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                    .setAccessKeyId(smsAccessKeyId)
                    .setAccessKeySecret(smsAccessKeySecret)
                    .setEndpoint(smsEndpoint);
            com.aliyun.dysmsapi20170525.Client client = new com.aliyun.dysmsapi20170525.Client(config);
            SendSmsRequest request = new SendSmsRequest()
                    .setPhoneNumbers(phone)
                    .setSignName(smsSignName)
                    .setTemplateCode(smsTemplateCode)
                    .setTemplateParam("{\"code\":\"" + verifyCode + "\"}");
            SendSmsResponse response = client.sendSms(request);
            String respCode = response.getBody() != null ? response.getBody().getCode() : "";
            if (!"OK".equalsIgnoreCase(respCode)) {
                String msg = response.getBody() != null ? response.getBody().getMessage() : "";
                log.warn("阿里云短信发送失败，phone={}, code={}, message={}", phone, respCode, msg);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("阿里云短信发送异常，phone={}", phone, e);
            return false;
        }
    }

    @Override
    public R<WxLoginResponse.UserInfo> getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return R.error("用户不存在");
        }

        WxLoginResponse.UserInfo userInfo = new WxLoginResponse.UserInfo();
        userInfo.setUserId(user.getId());
        userInfo.setNickName(user.getNickName());
        userInfo.setAvatarUrl(user.getAvatarUrl());
        userInfo.setPhone(user.getPhone());

        return R.ok(userInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<WxLoginResponse.UserInfo> updateUserInfo(Long userId, UpdateUserInfoRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return R.error("用户不存在");
        }

        // 更新昵称
        if (request.getNickName() != null && !request.getNickName().trim().isEmpty()) {
            String nickName = request.getNickName().trim();
            // 验证昵称长度
            if (nickName.length() < 1 || nickName.length() > 16) {
                return R.error("昵称长度必须在1-16个字符之间");
            }
            user.setNickName(nickName);
        }

        // 更新头像
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().trim().isEmpty()) {
            user.setAvatarUrl(request.getAvatarUrl().trim());
        }

        // 更新手机号
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            user.setPhone(request.getPhone().trim());
        }

        userMapper.updateById(user);

        WxLoginResponse.UserInfo userInfo = new WxLoginResponse.UserInfo();
        userInfo.setUserId(user.getId());
        userInfo.setNickName(user.getNickName());
        userInfo.setAvatarUrl(user.getAvatarUrl());
        userInfo.setPhone(user.getPhone());

        return R.ok(userInfo);
    }

    @Override
    public R<AuthController.UploadAvatarResponse> uploadAvatar(Long userId, MultipartFile file) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                return R.error("用户不存在");
            }

            // 以 userId 命名，避免 openid 暴露
            String fileUrl = ossService.uploadFileWithUserId(file, "avatars/client", userId);

            AuthController.UploadAvatarResponse response = new AuthController.UploadAvatarResponse();
            response.setUrl(fileUrl);

            return R.success(response);
        } catch (Exception e) {
            log.error("上传头像失败", e);
            return R.error("上传失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<WxLoginResponse> bindPhone(String code, String openid) {
        try {
            String accessToken = getWxAccessToken();
            if (accessToken == null) {
                return R.error("获取微信凭证失败");
            }

            String phone = getWxPhoneNumber(accessToken, code);
            if (!StringUtils.hasText(phone)) {
                return R.error("获取手机号失败");
            }

            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getOpenid, openid);
            User user = userMapper.selectOne(wrapper);

            if (user == null) {
                return registerUser(openid, "", phone);
            }
            user.setPhone(phone);
            userMapper.updateById(user);
            log.info("用户绑定手机号成功，userId: {}, phone: {}", user.getId(), phone);
            return R.ok(issueTokensAndBuildResponse(user, false));
        } catch (Exception e) {
            log.error("绑定手机号异常", e);
            return R.error("绑定失败，请重试");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    private R<WxLoginResponse> registerUser(String openid, String sessionKey, String phone) {
        try {
            User user = new User();
            user.setOpenid(openid);
            user.setSessionKey(sessionKey);
            user.setPhone(phone);
            user.setStatus(StatusEnum.ENABLE);
            userMapper.insert(user);
            applyDefaultNickAndAvatar(user);
            log.info("新用户注册成功，userId: {}, phone: {}", user.getId(), phone);
            return R.ok(issueTokensAndBuildResponse(user, true));
        } catch (Exception e) {
            log.error("用户注册异常", e);
            return R.error("注册失败，请重试");
        }
    }

    @Override
    public R<Void> logout(String token) {
        if (token != null && !token.isEmpty()) {
            try {
                // 从token中解析claims获取userId
                Claims claims = jwtUtil.parseToken(token);
                Object userIdObj = claims.get("userId");
                if (userIdObj != null) {
                    Long userId = Long.valueOf(userIdObj.toString());
                    // 删除Redis中的token
                    String redisKey = "client:token:" + userId;
                    redisUtil.delete(redisKey);
                    // 删除Redis中的刷新token
                    String refreshTokenKey = "client:refreshToken:" + userId;
                    redisUtil.delete(refreshTokenKey);
                    redisUtil.delete("user:token:map:" + token);
                }
                // 将token加入黑名单（可选）
                String blacklistKey = "client:token:blacklist:" + token;
                redisUtil.set(blacklistKey, "1", TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);
            } catch (Exception e) {
                log.error("退出登录异常", e);
            }
        }
        return R.success();
    }

    @Override
    public R<Map<String, String>> refreshToken(String refreshToken) {
        try {
            // 解析刷新token
            Claims claims = jwtUtil.parseToken(refreshToken);
            Object userIdObj = claims.get("userId");
            Object usernameObj = claims.get("username");
            Object typeObj = claims.get("type");

            // 验证刷新token的有效性
            if (userIdObj == null || usernameObj == null || !"refresh".equals(typeObj)) {
                return R.error("无效的刷新令牌");
            }

            Long userId = Long.valueOf(userIdObj.toString());
            String username = usernameObj.toString();

            // 验证刷新token是否在Redis中存在
            String refreshTokenKey = "client:refreshToken:" + userId;
            String storedRefreshToken = (String) redisUtil.get(refreshTokenKey);
            if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
                return R.error("刷新令牌已过期或已被注销");
            }

            // 生成新的访问token和刷新token
            String newToken = jwtUtil.generateToken(userId, username);
            String newRefreshToken = jwtUtil.generateRefreshToken(userId, username);

            // 更新Redis中的token
            String tokenKey = "client:token:" + userId;
            redisUtil.set(tokenKey, newToken, TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);
            redisUtil.set(refreshTokenKey, newRefreshToken, 7, TimeUnit.DAYS);
            redisUtil.set("user:token:map:" + newToken, userId, TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);

            // 组装响应
            Map<String, String> result = new HashMap<>();
            result.put("token", newToken);
            result.put("refreshToken", newRefreshToken);

            return R.ok(result);
        } catch (Exception e) {
            log.error("刷新令牌异常", e);
            return R.error("刷新令牌失败");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getWxSession(String code) {
        try {
            if (!StringUtils.hasText(wxAppId) || !StringUtils.hasText(wxSecret)) {
                log.warn("微信小程序 appid/secret 未配置，请设置 wx.miniapp.appid 与 wx.miniapp.secret");
                return null;
            }
            String url = String.format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    WX_LOGIN_URL, wxAppId, wxSecret, code);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String responseBody = response.getBody();
            if (responseBody == null) {
                log.warn("微信 code2session 响应体为空");
                return null;
            }

            Map<String, Object> body = jsonMapper.readValue(responseBody, Map.class);

            Object errcodeObj = body.get("errcode");
            if (errcodeObj != null) {
                int errcode = errcodeObj instanceof Number ? ((Number) errcodeObj).intValue() : Integer.parseInt(errcodeObj.toString());
                String errmsg = body.get("errmsg") != null ? body.get("errmsg").toString() : "";
                log.warn("微信 code2session 失败，errcode={}, errmsg={}，请检查 code 是否有效或 appid/secret 是否正确", errcode, errmsg);
                return null;
            }

            if (body.get("openid") != null) {
                Map<String, String> result = new HashMap<>();
                result.put("openid", body.get("openid").toString());
                Object sk = body.get("session_key");
                result.put("session_key", sk != null ? sk.toString() : "");
                return result;
            }
        } catch (Exception e) {
            log.error("获取微信 session 异常", e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private String getWxAccessToken() {
        try {
            String url = String.format("%s?grant_type=client_credential&appid=%s&secret=%s",
                    WX_ACCESS_TOKEN_URL, wxAppId, wxSecret);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getBody() != null) {
                Map<String, Object> body = jsonMapper.readValue(response.getBody(), Map.class);
                if (body.get("access_token") != null) {
                    return (String) body.get("access_token");
                }
            }
        } catch (Exception e) {
            log.error("获取微信access_token失败", e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private String getWxPhoneNumber(String accessToken, String code) {
        try {
            String url = WX_PHONE_URL + "?access_token=" + accessToken;

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("code", code);

            String jsonBody = jsonMapper.writeValueAsString(requestBody);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentLength(jsonBody.getBytes(StandardCharsets.UTF_8).length);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            String responseBody = response.getBody();
            if (responseBody == null) {
                log.warn("getuserphonenumber 响应体为空");
                return null;
            }
            Map<String, Object> body = jsonMapper.readValue(responseBody, Map.class);
            Object errcodeObj = body.get("errcode");
            if (errcodeObj != null) {
                int errcode = errcodeObj instanceof Number ? ((Number) errcodeObj).intValue() : Integer.parseInt(errcodeObj.toString());
                if (errcode != 0) {
                    String errmsg = body.get("errmsg") != null ? body.get("errmsg").toString() : "";
                    log.warn("getuserphonenumber 失败 errcode={}, errmsg={}", errcode, errmsg);
                    return null;
                }
            }
            if (body.get("phone_info") != null) {
                Map<String, Object> phoneInfo = (Map<String, Object>) body.get("phone_info");
                Object pn = phoneInfo.get("phoneNumber");
                if (pn != null) {
                    return pn.toString();
                }
                Object pure = phoneInfo.get("purePhoneNumber");
                if (pure != null) {
                    return pure.toString();
                }
                Object ph = phoneInfo.get("phone");
                if (ph != null) {
                    return ph.toString();
                }
            }
        } catch (Exception e) {
            log.error("获取微信手机号失败", e);
        }
        return null;
    }
}
