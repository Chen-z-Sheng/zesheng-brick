package com.zesheng.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置
 * 使用 JWT 认证，无状态会话
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final PubRecycleMarketInternalApiKeyFilter pubRecycleMarketInternalApiKeyFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            PubRecycleMarketInternalApiKeyFilter pubRecycleMarketInternalApiKeyFilter,
            JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.pubRecycleMarketInternalApiKeyFilter = pubRecycleMarketInternalApiKeyFilter;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 密码编码器
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 安全过滤链
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用默认的表单登录
                .formLogin(AbstractHttpConfigurer::disable)
                // 禁用 CSRF（开发环境）
                .csrf(AbstractHttpConfigurer::disable)
                // 禁用 HTTP Basic
                .httpBasic(AbstractHttpConfigurer::disable)
                // 配置会话管理为无状态
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置请求授权
                .authorizeHttpRequests(authorize -> authorize
                        // 放行所有OPTIONS预检请求
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        // 允许访问文档相关路径
                        .requestMatchers("/doc.html").permitAll()
                        .requestMatchers("/webjars/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/knife4j/**").permitAll()
                        // 允许访问登录注册路径
                        .requestMatchers("/auth/**").permitAll()
                        // 回收行情：由 PubRecycleMarketInternalApiKeyFilter 校验服务间密钥后再进入
                        .requestMatchers("/pub/recycle-market/**").permitAll()
                        // 其他请求需要认证
                        .anyRequest().authenticated()
                )
                // 服务间密钥（先于 JWT，对应 C 端 RestTemplate 请求头）
                .addFilterBefore(pubRecycleMarketInternalApiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}