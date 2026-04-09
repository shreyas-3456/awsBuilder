package com.awsBuilder.builder.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Cache configuration using Caffeine for catalog data caching.
 * Provides 5-minute TTL for catalog data to reduce database load.
 */
@Configuration
@EnableCaching
public class CacheConfiguration {

    /**
     * Configures Caffeine cache manager with 5-minute TTL.
     * Cache name: "catalogData"
     * TTL: 5 minutes (300 seconds)
     *
     * @return CacheManager configured with Caffeine
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("catalogData");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats());
        return cacheManager;
    }
}
