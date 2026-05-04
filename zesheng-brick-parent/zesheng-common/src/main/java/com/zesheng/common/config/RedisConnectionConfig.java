package com.zesheng.common.config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * Redis：Lettuce + commons-pool2，池参数来自 system.redis.pool
 */
@Configuration
public class RedisConnectionConfig {

    private static final int DEFAULT_MAX_TOTAL = 8;
    private static final int DEFAULT_MAX_IDLE = 8;
    private static final int DEFAULT_MIN_IDLE = 0;
    private static final long DEFAULT_MAX_WAIT_MS = 2_000L;
    private static final int DEFAULT_COMMAND_TIMEOUT_MS = 10_000;

    private final SystemConfig systemConfig;

    public RedisConnectionConfig(SystemConfig systemConfig) {
        this.systemConfig = systemConfig;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        SystemConfig.RedisConfig redisConfig = systemConfig.getRedis();

        RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration();
        if (redisConfig != null) {
            standaloneConfig.setHostName(redisConfig.getHost() != null ? redisConfig.getHost() : "127.0.0.1");
            standaloneConfig.setPort(redisConfig.getPort() != null ? redisConfig.getPort() : 6379);
            standaloneConfig.setDatabase(redisConfig.getDatabase() != null ? redisConfig.getDatabase() : 0);
            if (StringUtils.hasText(redisConfig.getPassword())) {
                standaloneConfig.setPassword(redisConfig.getPassword());
            }
        } else {
            standaloneConfig.setHostName("127.0.0.1");
            standaloneConfig.setPort(6379);
            standaloneConfig.setDatabase(0);
        }

        SystemConfig.RedisConfig.PoolConfig pool = redisConfig != null ? redisConfig.getPool() : null;
        int maxTotal = positiveInt(pool != null ? pool.getMaxActive() : null, DEFAULT_MAX_TOTAL);
        int maxIdle = positiveInt(pool != null ? pool.getMaxIdle() : null, DEFAULT_MAX_IDLE);
        int minIdle = nonNegativeInt(pool != null ? pool.getMinIdle() : null, DEFAULT_MIN_IDLE);
        long maxWaitMs = positiveLong(pool != null ? pool.getMaxWait() : null, DEFAULT_MAX_WAIT_MS);

        GenericObjectPoolConfig<?> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(maxTotal);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxWait(Duration.ofMillis(maxWaitMs));

        int commandTimeoutMs = redisConfig != null && redisConfig.getTimeout() != null && redisConfig.getTimeout() > 0
                ? redisConfig.getTimeout()
                : DEFAULT_COMMAND_TIMEOUT_MS;

        LettucePoolingClientConfiguration lettuceClient = LettucePoolingClientConfiguration.builder()
                .poolConfig(poolConfig)
                .commandTimeout(Duration.ofMillis(commandTimeoutMs))
                .build();

        LettuceConnectionFactory factory = new LettuceConnectionFactory(standaloneConfig, lettuceClient);
        factory.afterPropertiesSet();
        return factory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer();

        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(jsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jsonRedisSerializer);

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    private static int positiveInt(Integer value, int defaultVal) {
        return value != null && value > 0 ? value : defaultVal;
    }

    private static int nonNegativeInt(Integer value, int defaultVal) {
        return value != null && value >= 0 ? value : defaultVal;
    }

    private static long positiveLong(Integer value, long defaultVal) {
        return value != null && value > 0 ? value : defaultVal;
    }
}
