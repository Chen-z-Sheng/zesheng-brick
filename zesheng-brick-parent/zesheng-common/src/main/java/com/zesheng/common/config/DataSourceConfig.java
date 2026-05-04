package com.zesheng.common.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * 数据源：使用 HikariCP，池参数可由 system.database.hikari 覆盖
 */
@Configuration
public class DataSourceConfig {

    private static final int DEFAULT_MAX_POOL = 10;
    private static final int DEFAULT_MIN_IDLE = 2;
    private static final long DEFAULT_CONN_TIMEOUT_MS = 30_000L;
    private static final long DEFAULT_IDLE_TIMEOUT_MS = 600_000L;
    private static final long DEFAULT_MAX_LIFETIME_MS = 1_800_000L;

    private final SystemConfig systemConfig;

    public DataSourceConfig(SystemConfig systemConfig) {
        this.systemConfig = systemConfig;
    }

    @Bean
    public DataSource dataSource() {
        SystemConfig.DatabaseConfig dbConfig = systemConfig.getDatabase();
        if (dbConfig == null || dbConfig.getUrl() == null) {
            throw new IllegalStateException("system.database 未配置或 jdbcUrl 为空");
        }

        HikariConfig hc = new HikariConfig();
        hc.setJdbcUrl(dbConfig.getUrl());
        hc.setUsername(dbConfig.getUsername());
        hc.setPassword(dbConfig.getPassword());
        hc.setDriverClassName(dbConfig.getDriverClassName());

        SystemConfig.DatabaseConfig.HikariPoolProperties hp = dbConfig.getHikari();
        hc.setMaximumPoolSize(positiveInt(hp != null ? hp.getMaximumPoolSize() : null, DEFAULT_MAX_POOL));
        hc.setMinimumIdle(nonNegativeInt(hp != null ? hp.getMinimumIdle() : null, DEFAULT_MIN_IDLE));
        hc.setConnectionTimeout(positiveLong(hp != null ? hp.getConnectionTimeoutMs() : null, DEFAULT_CONN_TIMEOUT_MS));
        hc.setIdleTimeout(positiveLong(hp != null ? hp.getIdleTimeoutMs() : null, DEFAULT_IDLE_TIMEOUT_MS));
        hc.setMaxLifetime(positiveLong(hp != null ? hp.getMaxLifetimeMs() : null, DEFAULT_MAX_LIFETIME_MS));
        hc.setPoolName(blankToDefault(hp != null ? hp.getPoolName() : null, "zesheng-hikari"));

        hc.addDataSourceProperty("cachePrepStmts", "true");
        hc.addDataSourceProperty("prepStmtCacheSize", "250");
        hc.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hc.addDataSourceProperty("useServerPrepStmts", "true");

        return new HikariDataSource(hc);
    }

    private static int positiveInt(Integer value, int defaultVal) {
        return value != null && value > 0 ? value : defaultVal;
    }

    private static int nonNegativeInt(Integer value, int defaultVal) {
        return value != null && value >= 0 ? value : defaultVal;
    }

    private static long positiveLong(Long value, long defaultVal) {
        return value != null && value > 0 ? value : defaultVal;
    }

    private static String blankToDefault(String value, String defaultVal) {
        return value != null && !value.isBlank() ? value : defaultVal;
    }
}
