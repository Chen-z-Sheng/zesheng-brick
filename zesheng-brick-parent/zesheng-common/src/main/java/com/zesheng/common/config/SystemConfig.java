package com.zesheng.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 系统配置类
 * 管理系统所有配置信息
 */
@Data
@Component
@ConfigurationProperties(prefix = "system")
public class SystemConfig {
    
    /**
     * JWT配置
     */
    private JwtConfig jwt;
    
    /**
     * 数据库配置
     */
    private DatabaseConfig database;
    
    /**
     * Redis配置
     */
    private RedisConfig redis;
    
    /**
     * JWT配置内部类
     */
    @Data
    public static class JwtConfig {
        private String secret;
        private Long expire;
        private Long refreshExpire;
    }
    
    /**
     * 数据库配置内部类
     */
    @Data
    public static class DatabaseConfig {
        private String url;
        private String username;
        private String password;
        private String driverClassName;
        /** HikariCP 连接池；未配置时由数据源 Bean 使用内置默认值 */
        private HikariPoolProperties hikari;

        @Data
        public static class HikariPoolProperties {
            private Integer maximumPoolSize;
            private Integer minimumIdle;
            private Long connectionTimeoutMs;
            private Long idleTimeoutMs;
            private Long maxLifetimeMs;
            private String poolName;
        }
    }
    
    /**
     * Redis配置内部类
     */
    @Data
    public static class RedisConfig {
        private String host;
        private Integer port;
        private String password;
        private Integer database;
        private Integer timeout;
        private PoolConfig pool;
        
        /**
         * 连接池配置
         */
        @Data
        public static class PoolConfig {
            private Integer maxActive;
            private Integer maxIdle;
            private Integer minIdle;
            private Integer maxWait;
        }
    }
}
