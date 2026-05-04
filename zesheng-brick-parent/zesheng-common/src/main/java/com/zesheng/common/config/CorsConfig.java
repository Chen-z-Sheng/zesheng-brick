package com.zesheng.common.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置
 * 处理跨域请求
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 允许所有路径
                .allowedOriginPatterns("*") // 允许所有来源
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") // 允许的HTTP方法
                .allowedHeaders("*") // 允许所有请求头
                .exposedHeaders("Content-Disposition") // 暴露的响应头
                .allowCredentials(true) // 允许携带凭证
                .maxAge(3600); // 预检请求有效期（秒）
    }

    /**
     * OPTIONS 预检请求在 DispatcherServlet 中没有映射会返回 404，导致 CORS 失败。
     * 用过滤器在最先处理 OPTIONS，直接返回 200 + CORS 头。
     */
    @Bean
    public FilterRegistrationBean<OptionsCorsFilter> optionsCorsFilterRegistration() {
        FilterRegistrationBean<OptionsCorsFilter> registration = new FilterRegistrationBean<>(new OptionsCorsFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.setName("optionsCorsFilter");
        return registration;
    }
}
