package com.zesheng.client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * C 端出站 HTTP（微信、管理端转发等）超时配置
 */
@Data
@ConfigurationProperties(prefix = "zesheng.http-client")
public class ClientHttpProperties {

    /** 连接超时 */
    private Duration connectTimeout = Duration.ofSeconds(5);

    /** 读超时 */
    private Duration readTimeout = Duration.ofSeconds(20);
}
