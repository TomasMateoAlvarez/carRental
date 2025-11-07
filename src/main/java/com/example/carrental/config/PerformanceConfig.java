package com.example.carrental.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import java.util.Arrays;
import java.util.concurrent.Executor;

@Configuration
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.example.carrental.repository")
public class PerformanceConfig {

    /**
     * Configure caching for better performance
     */
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(Arrays.asList(
            "vehicles",
            "users",
            "reservations",
            "dashboard-kpis",
            "maintenance-records",
            "notifications"
        ));
        cacheManager.setAllowNullValues(false);
        return cacheManager;
    }

    /**
     * Configure async task executor for non-blocking operations
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("CarRental-Async-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * Configure request logging for performance monitoring in development
     */
    @Bean
    @Profile("!production")
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
        loggingFilter.setIncludeClientInfo(true);
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludePayload(false); // Don't log sensitive data
        loggingFilter.setIncludeHeaders(false);
        loggingFilter.setMaxPayloadLength(1000);
        return loggingFilter;
    }

    /**
     * Database connection pool optimization properties
     */
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public static class HikariProperties {
        private int maximumPoolSize = 20;
        private int minimumIdle = 5;
        private long connectionTimeout = 30000;
        private long idleTimeout = 600000;
        private long maxLifetime = 1800000;
        private boolean autoCommit = true;
        private String connectionTestQuery = "SELECT 1";

        // Getters and setters
        public int getMaximumPoolSize() { return maximumPoolSize; }
        public void setMaximumPoolSize(int maximumPoolSize) { this.maximumPoolSize = maximumPoolSize; }

        public int getMinimumIdle() { return minimumIdle; }
        public void setMinimumIdle(int minimumIdle) { this.minimumIdle = minimumIdle; }

        public long getConnectionTimeout() { return connectionTimeout; }
        public void setConnectionTimeout(long connectionTimeout) { this.connectionTimeout = connectionTimeout; }

        public long getIdleTimeout() { return idleTimeout; }
        public void setIdleTimeout(long idleTimeout) { this.idleTimeout = idleTimeout; }

        public long getMaxLifetime() { return maxLifetime; }
        public void setMaxLifetime(long maxLifetime) { this.maxLifetime = maxLifetime; }

        public boolean isAutoCommit() { return autoCommit; }
        public void setAutoCommit(boolean autoCommit) { this.autoCommit = autoCommit; }

        public String getConnectionTestQuery() { return connectionTestQuery; }
        public void setConnectionTestQuery(String connectionTestQuery) { this.connectionTestQuery = connectionTestQuery; }
    }

    /**
     * JPA/Hibernate performance properties
     */
    @ConfigurationProperties(prefix = "spring.jpa.properties.hibernate")
    public static class HibernateProperties {
        private boolean useSecondLevelCache = true;
        private boolean useQueryCache = true;
        private String cacheRegionFactory = "org.hibernate.cache.jcache.JCacheRegionFactory";
        private int jdbcBatchSize = 20;
        private boolean orderInserts = true;
        private boolean orderUpdates = true;
        private boolean batchVersionedData = true;
        private boolean generateStatistics = false;

        // Getters and setters
        public boolean isUseSecondLevelCache() { return useSecondLevelCache; }
        public void setUseSecondLevelCache(boolean useSecondLevelCache) { this.useSecondLevelCache = useSecondLevelCache; }

        public boolean isUseQueryCache() { return useQueryCache; }
        public void setUseQueryCache(boolean useQueryCache) { this.useQueryCache = useQueryCache; }

        public String getCacheRegionFactory() { return cacheRegionFactory; }
        public void setCacheRegionFactory(String cacheRegionFactory) { this.cacheRegionFactory = cacheRegionFactory; }

        public int getJdbcBatchSize() { return jdbcBatchSize; }
        public void setJdbcBatchSize(int jdbcBatchSize) { this.jdbcBatchSize = jdbcBatchSize; }

        public boolean isOrderInserts() { return orderInserts; }
        public void setOrderInserts(boolean orderInserts) { this.orderInserts = orderInserts; }

        public boolean isOrderUpdates() { return orderUpdates; }
        public void setOrderUpdates(boolean orderUpdates) { this.orderUpdates = orderUpdates; }

        public boolean isBatchVersionedData() { return batchVersionedData; }
        public void setBatchVersionedData(boolean batchVersionedData) { this.batchVersionedData = batchVersionedData; }

        public boolean isGenerateStatistics() { return generateStatistics; }
        public void setGenerateStatistics(boolean generateStatistics) { this.generateStatistics = generateStatistics; }
    }
}