package com.zesheng.common.util;

import com.zesheng.common.config.SystemConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT工具类
 */
@Component
public class JwtUtil {
    
    private final SystemConfig systemConfig;

    @Autowired
    public JwtUtil(SystemConfig systemConfig) {
        this.systemConfig = systemConfig;
    }

    // 生成token
    public String generateToken(Long userId, String username) {
        // 生成加密密钥
        String secret = getJwtSecret();
        Long expire = getJwtExpire();
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        // 构建token
        return Jwts.builder()
                .claim("userId", userId) // 自定义载荷：用户ID
                .claim("username", username) // 自定义载荷：用户名
                .setIssuedAt(new Date()) // 签发时间
                .setExpiration(new Date(System.currentTimeMillis() + expire)) // 过期时间
                .signWith(key) // 签名
                .compact();
    }

    // 生成刷新令牌
    public String generateRefreshToken(Long userId, String username) {
        String secret = getJwtSecret();
        Long refreshExpire = getJwtRefreshExpire();
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.builder()
                .claim("userId", userId)
                .claim("username", username)
                .claim("type", "refresh") // 标记为刷新令牌
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpire))
                .signWith(key)
                .compact();
    }

    // 解析token获取载荷
    public Claims parseToken(String token) {
        String secret = getJwtSecret();
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 从token中获取用户ID
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        Object userIdObj = claims.get("userId");
        if (userIdObj != null) {
            return Long.valueOf(userIdObj.toString());
        }
        return null;
    }

    // 从token中获取用户名
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }
    
    // 获取过期时间
    public Long getExpire() {
        return getJwtExpire();
    }
    
    // 获取刷新令牌过期时间
    public Long getRefreshExpire() {
        return getJwtRefreshExpire();
    }
    
    // 获取JWT密钥
    private String getJwtSecret() {
        if (systemConfig != null && systemConfig.getJwt() != null && systemConfig.getJwt().getSecret() != null) {
            return systemConfig.getJwt().getSecret();
        }
        // 默认密钥
        return "CZK_2026_8888_CZK_2026_8888_CZK_2026_8888";
    }
    
    // 获取JWT过期时间
    private Long getJwtExpire() {
        if (systemConfig != null && systemConfig.getJwt() != null && systemConfig.getJwt().getExpire() != null) {
            return systemConfig.getJwt().getExpire();
        }
        // 默认过期时间：2小时
        return 7200000L;
    }
    
    // 获取JWT刷新令牌过期时间
    private Long getJwtRefreshExpire() {
        if (systemConfig != null && systemConfig.getJwt() != null && systemConfig.getJwt().getRefreshExpire() != null) {
            return systemConfig.getJwt().getRefreshExpire();
        }
        // 默认刷新令牌过期时间：7天
        return 604800000L;
    }
}