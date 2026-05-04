package com.zesheng.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zesheng.admin.entity.User;
import com.zesheng.admin.mapper.UserMapper;
import com.zesheng.admin.model.request.LoginRequest;
import com.zesheng.admin.model.response.LoginResponse;
import com.zesheng.admin.service.IAuthService;
import com.zesheng.common.enums.StatusEnum;
import com.zesheng.common.util.JwtUtil;
import com.zesheng.common.util.RedisUtil;
import com.zesheng.common.enums.ResultCodeEnum;
import com.zesheng.common.response.R;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 登录认证业务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements IAuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RedisUtil redisUtil;

    @Override
    public R<LoginResponse> login(LoginRequest loginRequest, HttpServletRequest httpRequest) {
        // 兜底校验DTO非空
        if (loginRequest == null || loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
            return R.error(ResultCodeEnum.PARAM_EMPTY);
        }

        try {
            // 根据用户名查询用户
            User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", loginRequest.getUsername()).isNull("deleted_at"));
            if (user == null) {
                log.warn("登录失败：用户名{}不存在", loginRequest.getUsername());
                return R.error(ResultCodeEnum.USER_NOT_EXIST);
            }

            // 校验用户状态（是否禁用）
            if (user.getStatus() == StatusEnum.DISABLE) {
                log.warn("登录失败：用户名{}已禁用", loginRequest.getUsername());
                return R.error(ResultCodeEnum.USER_DISABLED);
            }

            // 校验密码
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
                log.warn("登录失败：用户名{}密码错误", loginRequest.getUsername());
                return R.error(ResultCodeEnum.PASSWORD_ERROR);
            }

            // 生成JWT访问令牌和刷新令牌
            String token = jwtUtil.generateToken(user.getId(), user.getUsername());
            String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());
            log.info("用户名{}登录成功，生成token：{}，refreshToken：{}", loginRequest.getUsername(), token, refreshToken);

            // 记录最近登录时间与 IP（供个人中心展示）
            String clientIp = resolveClientIp(httpRequest);
            userMapper.update(null, new LambdaUpdateWrapper<User>()
                    .eq(User::getId, user.getId())
                    .set(User::getLastLoginAt, LocalDateTime.now())
                    .set(User::getLastLoginIp, clientIp));

            // 将token存入Redis，使用Hash结构优化存储
            // 访问令牌：user:token:access:{userId} -> {token: expireTime}
            // 刷新令牌：user:token:refresh:{userId} -> {token: expireTime}
            redisUtil.set("user:token:access:" + user.getId(), token, jwtUtil.getExpire(), TimeUnit.MILLISECONDS);
            redisUtil.set("user:token:refresh:" + user.getId(), refreshToken, jwtUtil.getRefreshExpire(), TimeUnit.MILLISECONDS);
            // 同时存储token到userId的映射，用于快速查找
            redisUtil.set("user:token:map:" + token, user.getId(), jwtUtil.getExpire(), TimeUnit.MILLISECONDS);
            redisUtil.set("user:token:map:" + refreshToken, user.getId(), jwtUtil.getRefreshExpire(), TimeUnit.MILLISECONDS);

            // 构建登录响应
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setAccessToken(token);
            loginResponse.setTokenType("Bearer");
            // 转换过期时间为秒
            loginResponse.setExpiresIn(jwtUtil.getExpire() != null ? Math.toIntExact(jwtUtil.getExpire() / 1000) : 7200);
            // 设置刷新令牌
            loginResponse.setRefreshToken(refreshToken);

            // 返回登录响应
            return R.success(loginResponse);
        } catch (Exception e) {
            log.error("用户名{}登录异常", loginRequest.getUsername(), e);
            return R.error(ResultCodeEnum.FAIL);
        }
    }

    /**
     * 刷新令牌
     * @param refreshToken 刷新令牌
     * @return  R<LoginResponse> 登录响应
     */
    @Override
    public R<LoginResponse> refreshToken(String refreshToken) {
        // 兜底校验刷新令牌非空
        if (refreshToken == null) {
            return R.error(ResultCodeEnum.PARAM_EMPTY);
        }

        try {
            // 解析刷新令牌，获取用户信息
            Claims claims = jwtUtil.parseToken(refreshToken);
            Long userId = claims.get("userId", Long.class);
            String username = claims.get("username", String.class);
            String tokenType = claims.get("type", String.class);

            // 校验是否为刷新令牌
            if (!"refresh".equals(tokenType)) {
                log.warn("刷新令牌无效：令牌类型不正确，type={}", tokenType);
                return R.error(ResultCodeEnum.TOKEN_INVALID);
            }

            if (userId == null || username == null) {
                log.warn("刷新令牌无效：缺少用户信息");
                return R.error(ResultCodeEnum.TOKEN_INVALID);
            }

            // 根据用户ID查询用户，确保用户存在且未被禁用
            User user = userMapper.selectOne(new QueryWrapper<User>().eq("id", userId).isNull("deleted_at"));
            if (user == null) {
                log.warn("刷新令牌无效：用户不存在，userId={}", userId);
                return R.error(ResultCodeEnum.USER_NOT_EXIST);
            }

            // 校验用户状态（是否禁用）
            if (user.getStatus() == StatusEnum.DISABLE) {
                log.warn("刷新令牌无效：用户已禁用，userId={}", userId);
                return R.error(ResultCodeEnum.USER_DISABLED);
            }

            // 删除旧的刷新令牌
            redisUtil.delete("user:token:refresh:" + userId);
            redisUtil.delete("user:token:map:" + refreshToken);
            
            // 生成新的访问令牌和刷新令牌
            String newToken = jwtUtil.generateToken(userId, username);
            String newRefreshToken = jwtUtil.generateRefreshToken(userId, username);
            log.info("用户{}刷新令牌成功，生成新token：{}，新refreshToken：{}", username, newToken, newRefreshToken);

            // 将新的token存入Redis
            redisUtil.set("user:token:access:" + userId, newToken, jwtUtil.getExpire(), TimeUnit.MILLISECONDS);
            redisUtil.set("user:token:refresh:" + userId, newRefreshToken, jwtUtil.getRefreshExpire(), TimeUnit.MILLISECONDS);
            // 同时存储token到userId的映射，用于快速查找
            redisUtil.set("user:token:map:" + newToken, userId, jwtUtil.getExpire(), TimeUnit.MILLISECONDS);
            redisUtil.set("user:token:map:" + newRefreshToken, userId, jwtUtil.getRefreshExpire(), TimeUnit.MILLISECONDS);

            // 构建登录响应
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setAccessToken(newToken);
            loginResponse.setTokenType("Bearer");
            // 转换过期时间为秒
            loginResponse.setExpiresIn(jwtUtil.getExpire() != null ? Math.toIntExact(jwtUtil.getExpire() / 1000) : 7200);
            // 设置新的刷新令牌
            loginResponse.setRefreshToken(newRefreshToken);

            // 返回登录响应
            return R.success(loginResponse);
        } catch (Exception e) {
            log.error("刷新令牌异常", e);
            return R.error(ResultCodeEnum.TOKEN_INVALID);
        }
    }

    @Override
    public R<Void> logout(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return R.success();
        }
        try {
            Claims claims = jwtUtil.parseToken(accessToken);
            Long userId = claims.get("userId", Long.class);
            if (userId == null) {
                return R.success();
            }
            Object refreshTokenObj = redisUtil.get("user:token:refresh:" + userId);
            String refreshToken = refreshTokenObj != null ? refreshTokenObj.toString() : null;
            redisUtil.delete("user:token:access:" + userId);
            redisUtil.delete("user:token:refresh:" + userId);
            redisUtil.delete("user:token:map:" + accessToken);
            if (refreshToken != null) {
                redisUtil.delete("user:token:map:" + refreshToken);
            }
            log.info("用户{}退出登录，令牌已失效", userId);
        } catch (Exception e) {
            log.debug("退出登录时解析令牌失败（可能已过期），忽略: {}", e.getMessage());
        }
        return R.success();
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String xff = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xff)) {
            return xff.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp.trim();
        }
        String remote = request.getRemoteAddr();
        return remote != null ? remote : "";
    }
}