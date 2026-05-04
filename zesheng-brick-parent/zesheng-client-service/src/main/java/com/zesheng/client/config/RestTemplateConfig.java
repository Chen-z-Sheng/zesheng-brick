package com.zesheng.client.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP 客户端（调用微信开放平台、管理端开放接口等）
 */
@Configuration
@EnableConfigurationProperties(ClientHttpProperties.class)
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder, ClientHttpProperties props) {
        return builder
                .setConnectTimeout(props.getConnectTimeout())
                .setReadTimeout(props.getReadTimeout())
                .build();
    }
}
