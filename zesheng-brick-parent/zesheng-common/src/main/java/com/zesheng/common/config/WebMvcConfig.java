package com.zesheng.common.config;

import com.zesheng.common.interceptor.AuthInterceptor;
import com.zesheng.common.interceptor.LogInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final LogInterceptor logInterceptor;
    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/actuator/**",
                        "/doc.html",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/knife4j/**"
                );
        
        // 认证拦截器（需登录的接口由各服务自行校验：admin 用 Redis user:token:map，client 用 JWT Filter + client:token:userId）
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // 登录相关接口
                        "/auth/login",
                        "/auth/refresh",
                        "/auth/refresh-token",
                        "/auth/wx-login",
                        "/auth/wx-phone-login",
                        "/auth/donut-login",
                        "/auth/send-sms-code",
                        "/auth/sms-login",
                        "/auth/bind-phone",
                        // 小程序端：由 JwtAuthenticationFilter 校验，Controller 内根据 request 属性判断
                        "/auth/current-user",
                        "/auth/update-user-info",
                        "/auth/upload-avatar",
                        "/auth/logout",
                        // 帮助中心FAQ（公开接口，无需登录）
                        "/help-faq/**",
                        // 公开系统配置（白名单 key，与 C 端 Security permitAll 一致；不经 AuthInterceptor）
                        "/public/sys-config/**",
                        // 首页轮播图（公开接口，无需登录）
                        "/banner/**",
                        // 物流公司名称联想（参考数据，C 端 permitAll）
                        "/logistics-company/**",
                        // 小程序端回收行情（C 端 Security 已 permitAll；无 Token 时不经 Redis 校验）
                        "/recycle-market/**",
                        // 公告历史（首页滚动条、历史页；C 端 Security 已 permitAll）
                        "/announcements/**",
                        // 管理端行情开放查询（供 C 端 RestTemplate 内网调用，无管理端 Bearer）
                        "/pub/recycle-market/**",
                        // 文档相关
                        "/doc.html",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/knife4j/**",
                        "/swagger-ui.html",
                        "/actuator/**"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        
        registry.addResourceHandler("/v3/api-docs/**")
                .addResourceLocations("classpath:/META-INF/resources/");
        
        registry.addResourceHandler("/knife4j/**")
                .addResourceLocations("classpath:/META-INF/resources/knife4j/");
        
        registry.addResourceHandler("/swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
    }


}