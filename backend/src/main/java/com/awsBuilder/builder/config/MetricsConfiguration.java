package com.awsBuilder.builder.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for performance monitoring and metrics.
 * 
 * This configuration sets up Micrometer metrics for monitoring:
 * - Log write latency
 * - SQL query interception overhead
 * - Trace context propagation time
 * 
 * Metrics are exposed via Spring Boot Actuator at /actuator/metrics
 * 
 * Requirements: 14.2, 14.3, 14.4, 14.5
 */
@Configuration
public class MetricsConfiguration {

    /**
     * Creates a timer for measuring log write latency.
     * 
     * This timer tracks the time taken to write log entries to files or console.
     * Used to monitor logging performance and detect bottlenecks.
     * 
     * @param meterRegistry the Micrometer meter registry
     * @return Timer for log write latency
     */
    @Bean
    public Timer logWriteLatencyTimer(MeterRegistry meterRegistry) {
        return Timer.builder("logging.write.latency")
                .description("Time taken to write log entries")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }

    /**
     * Creates a timer for measuring SQL query interception overhead.
     * 
     * This timer tracks the time added by P6Spy SQL interception and logging.
     * Used to monitor SQL logging performance impact.
     * 
     * @param meterRegistry the Micrometer meter registry
     * @return Timer for SQL interception overhead
     */
    @Bean
    public Timer sqlInterceptionOverheadTimer(MeterRegistry meterRegistry) {
        return Timer.builder("sql.interception.overhead")
                .description("Overhead added by SQL query interception and logging")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }

    /**
     * Creates a timer for measuring trace context propagation time.
     * 
     * This timer tracks the time taken to propagate trace context across threads
     * and layers. Used to monitor tracing performance impact.
     * 
     * @param meterRegistry the Micrometer meter registry
     * @return Timer for trace context propagation
     */
    @Bean
    public Timer traceContextPropagationTimer(MeterRegistry meterRegistry) {
        return Timer.builder("tracing.context.propagation")
                .description("Time taken to propagate trace context")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }
}
