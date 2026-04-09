package com.awsBuilder.builder.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for SQL query logging.
 * 
 * This class provides externalized configuration for SQL logging behavior,
 * allowing customization without code changes.
 * 
 * Properties:
 * - enabled: Whether SQL logging is enabled (default: true)
 * - slowQueryThreshold: Execution time threshold for WARN level logging in milliseconds (default: 1000)
 * 
 * Example usage in application.properties:
 * <pre>
 * sql.logging.enabled=true
 * sql.logging.slow-query-threshold=1000
 * </pre>
 * 
 * Requirements: 12.4, 12.6
 */
@Component
@ConfigurationProperties(prefix = "sql.logging")
public class SqlLoggingProperties {

    /**
     * Whether SQL query logging is enabled.
     * Default: true
     */
    private boolean enabled = true;

    /**
     * Execution time threshold for WARN level logging in milliseconds.
     * Queries exceeding this threshold are logged at WARN level.
     * Default: 1000 milliseconds
     */
    private long slowQueryThreshold = 1000;

    /**
     * Gets whether SQL logging is enabled.
     * 
     * @return true if SQL logging is enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether SQL logging is enabled.
     * 
     * @param enabled true to enable SQL logging, false to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets the slow query threshold in milliseconds.
     * 
     * @return slow query threshold in milliseconds
     */
    public long getSlowQueryThreshold() {
        return slowQueryThreshold;
    }

    /**
     * Sets the slow query threshold in milliseconds.
     * 
     * @param slowQueryThreshold slow query threshold in milliseconds
     */
    public void setSlowQueryThreshold(long slowQueryThreshold) {
        this.slowQueryThreshold = slowQueryThreshold;
    }
}
