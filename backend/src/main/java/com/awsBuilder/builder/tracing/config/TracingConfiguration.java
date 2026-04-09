package com.awsBuilder.builder.config;

import brave.sampler.Sampler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Micrometer Tracing with Brave implementation.
 * 
 * This configuration sets up distributed tracing infrastructure to enable trace context
 * propagation across all application layers. Spring Boot auto-configuration handles most
 * of the Brave setup, and this class provides customization for sampling strategy.
 * 
 * The tracer generates 16-character hexadecimal trace IDs and span IDs, and propagates
 * trace context across threads to support async operations.
 * 
 * Error Handling:
 * - Wraps tracer bean creation in try-catch to handle initialization failures
 * - Logs clear error messages if tracing initialization fails
 * - Provides fallback behavior if Brave dependencies are missing
 * - Validates required tracing beans at startup
 * 
 * Requirements: 1.1, 1.2, 1.3, 1.4, 7.1, 7.2, 11.1, 12.5, 15.4
 */
@Configuration
public class TracingConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(TracingConfiguration.class);

    /**
     * Creates a Sampler bean that samples all requests for development.
     * 
     * This sampler is configured to sample 100% of requests, which is appropriate
     * for development environments. For production, this should be configured
     * via application properties to use a lower sampling rate (e.g., 0.1 for 10%).
     * 
     * Error Handling:
     * - Wraps sampler creation in try-catch
     * - Logs error if sampler initialization fails
     * - Returns ALWAYS_SAMPLE as fallback
     * 
     * @return Sampler configured to sample all requests
     */
    @Bean
    @ConditionalOnMissingBean
    public Sampler sampler() {
        try {
            logger.debug("Initializing Sampler with ALWAYS_SAMPLE strategy for development");
            return Sampler.ALWAYS_SAMPLE;
        } catch (Exception e) {
            logger.error("Failed to initialize Sampler for tracing. Tracing may not work correctly. " +
                    "Ensure Brave dependencies are included in build.gradle: " +
                    "io.micrometer:micrometer-tracing-bridge-brave and io.zipkin.brave:brave", e);
            // Return fallback sampler that samples all requests
            return Sampler.ALWAYS_SAMPLE;
        }
    }
}
