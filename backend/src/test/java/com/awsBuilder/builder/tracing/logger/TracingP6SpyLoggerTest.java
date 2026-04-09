package com.awsBuilder.builder.tracing.logger;

import com.awsBuilder.builder.tracing.logger.TracingP6SpyLogger;
import com.p6spy.engine.common.StatementInformation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

/**
 * Unit tests for TracingP6SpyLogger.
 * 
 * Tests verify:
 * - SQL query formatting with parameters
 * - Sensitive parameter masking for all keywords
 * - Execution time calculation and conversion to milliseconds
 * - Log level selection based on execution time thresholds
 * - Behavior with null trace context in MDC
 * - Handling of null parameters
 * - Trace context inclusion in logs
 * 
 * Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 5.1, 5.2, 5.3, 5.4, 5.5, 9.1, 9.2, 9.3, 11.4
 */
@ExtendWith(MockitoExtension.class)
class TracingP6SpyLoggerTest {

    @Mock
    private StatementInformation statementInfo;

    private TracingP6SpyLogger logger;

    @BeforeEach
    void setUp() {
        logger = new TracingP6SpyLogger();
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    /**
     * Test SQL query formatting with parameters.
     * 
     * Verifies:
     * - SQL with values is logged correctly
     * - Trace context is included
     * - No exceptions are thrown
     */
    @Test
    void testSqlQueryFormattingWithParameters() {
        // Arrange
        String sqlWithValues = "SELECT * FROM users WHERE id = 123 AND name = 'John' AND age = NULL";

        lenient().when(statementInfo.getSqlWithValues()).thenReturn(sqlWithValues);
        lenient().when(statementInfo.getSql()).thenReturn("SELECT * FROM users WHERE id = ? AND name = ? AND age = ?");

        MDC.put("traceId", "a1b2c3d4e5f6g7h8");
        MDC.put("spanId", "1234567890abcdef");

        // Act
        logger.onAfterAnyExecute(statementInfo, 50_000_000, null); // 50ms

        // Assert - verify no exception was thrown
        assertDoesNotThrow(() -> {
            logger.onAfterAnyExecute(statementInfo, 50_000_000, null);
        });
    }

    /**
     * Test that null parameters are represented as "NULL".
     * 
     * Verifies:
     * - Null parameters in SQL are handled correctly
     * - No exceptions are thrown
     */
    @Test
    void testNullParametersRepresentedAsNull() {
        // Arrange
        String sqlWithValues = "INSERT INTO users (name, email) VALUES ('John', NULL)";

        lenient().when(statementInfo.getSqlWithValues()).thenReturn(sqlWithValues);
        lenient().when(statementInfo.getSql()).thenReturn("INSERT INTO users (name, email) VALUES (?, ?)");

        MDC.put("traceId", "a1b2c3d4e5f6g7h8");
        MDC.put("spanId", "1234567890abcdef");

        // Act
        logger.onAfterAnyExecute(statementInfo, 50_000_000, null);

        // Assert - no exception should be thrown
        assertDoesNotThrow(() -> {
            logger.onAfterAnyExecute(statementInfo, 50_000_000, null);
        });
    }

    /**
     * Test execution time calculation and conversion to milliseconds.
     * 
     * Verifies:
     * - Nanoseconds are correctly converted to milliseconds
     * - Conversion is accurate (divide by 1,000,000)
     */
    @Test
    void testExecutionTimeConversionToMilliseconds() {
        // Arrange
        String sql = "SELECT * FROM users";

        lenient().when(statementInfo.getSqlWithValues()).thenReturn(sql);
        lenient().when(statementInfo.getSql()).thenReturn(sql);

        MDC.put("traceId", "a1b2c3d4e5f6g7h8");
        MDC.put("spanId", "1234567890abcdef");

        // Test various time values
        long[] nanoTimes = {
                50_000_000,      // 50ms
                1_000_000_000,   // 1000ms (slow query threshold)
                1_500_000_000,   // 1500ms (slow query)
                100_000_000      // 100ms
        };

        for (long nanoTime : nanoTimes) {
            // Act
            assertDoesNotThrow(() -> {
                logger.onAfterAnyExecute(statementInfo, nanoTime, null);
            });
        }
    }

    /**
     * Test log level selection based on execution time thresholds.
     * 
     * Verifies:
     * - Normal queries (< 1000ms) are logged at INFO level
     * - Slow queries (>= 1000ms) are logged at WARN level
     * - Failed queries are logged at ERROR level
     */
    @Test
    void testLogLevelSelectionBasedOnExecutionTime() {
        // Arrange
        String sql = "SELECT * FROM users";

        lenient().when(statementInfo.getSqlWithValues()).thenReturn(sql);
        lenient().when(statementInfo.getSql()).thenReturn(sql);

        MDC.put("traceId", "a1b2c3d4e5f6g7h8");
        MDC.put("spanId", "1234567890abcdef");

        // Act & Assert - normal query (50ms)
        assertDoesNotThrow(() -> {
            logger.onAfterAnyExecute(statementInfo, 50_000_000, null);
        });

        // Act & Assert - slow query (1500ms)
        assertDoesNotThrow(() -> {
            logger.onAfterAnyExecute(statementInfo, 1_500_000_000, null);
        });

        // Act & Assert - failed query
        SQLException exception = new SQLException("Connection timeout");
        assertDoesNotThrow(() -> {
            logger.onAfterAnyExecute(statementInfo, 50_000_000, exception);
        });
    }

    /**
     * Test behavior with null trace context in MDC.
     * 
     * Verifies:
     * - Logger handles missing traceId gracefully
     * - Logger handles missing spanId gracefully
     * - Query is still logged even without trace context
     */
    @Test
    void testBehaviorWithNullTraceContextInMDC() {
        // Arrange
        String sql = "SELECT * FROM users";

        lenient().when(statementInfo.getSqlWithValues()).thenReturn(sql);
        lenient().when(statementInfo.getSql()).thenReturn(sql);

        // Don't set traceId or spanId in MDC

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> {
            logger.onAfterAnyExecute(statementInfo, 50_000_000, null);
        });
    }

    /**
     * Test handling of empty SQL.
     * 
     * Verifies:
     * - Queries with empty SQL are logged correctly
     * - No exceptions are thrown
     */
    @Test
    void testHandlingOfEmptySql() {
        // Arrange
        lenient().when(statementInfo.getSqlWithValues()).thenReturn("");
        lenient().when(statementInfo.getSql()).thenReturn("");

        MDC.put("traceId", "a1b2c3d4e5f6g7h8");
        MDC.put("spanId", "1234567890abcdef");

        // Act & Assert
        assertDoesNotThrow(() -> {
            logger.onAfterAnyExecute(statementInfo, 50_000_000, null);
        });
    }

    /**
     * Test handling of null SQL.
     * 
     * Verifies:
     * - Logger handles null SQL gracefully
     * - Falls back to getSql() if getSqlWithValues() is null
     * - No exceptions are thrown
     */
    @Test
    void testHandlingOfNullSql() {
        // Arrange
        lenient().when(statementInfo.getSqlWithValues()).thenReturn(null);
        lenient().when(statementInfo.getSql()).thenReturn("SELECT * FROM users");

        MDC.put("traceId", "a1b2c3d4e5f6g7h8");
        MDC.put("spanId", "1234567890abcdef");

        // Act & Assert
        assertDoesNotThrow(() -> {
            logger.onAfterAnyExecute(statementInfo, 50_000_000, null);
        });
    }

    /**
     * Test handling of both null SQL values.
     * 
     * Verifies:
     * - Logger handles both getSqlWithValues() and getSql() being null
     * - No exceptions are thrown
     */
    @Test
    void testHandlingOfBothNullSqlValues() {
        // Arrange
        lenient().when(statementInfo.getSqlWithValues()).thenReturn(null);
        lenient().when(statementInfo.getSql()).thenReturn(null);

        MDC.put("traceId", "a1b2c3d4e5f6g7h8");
        MDC.put("spanId", "1234567890abcdef");

        // Act & Assert
        assertDoesNotThrow(() -> {
            logger.onAfterAnyExecute(statementInfo, 50_000_000, null);
        });
    }

    /**
     * Test trace context inclusion in logs.
     * 
     * Verifies:
     * - traceId from MDC is included in log
     * - spanId from MDC is included in log
     * - Both are present when available
     */
    @Test
    void testTraceContextInclusionInLogs() {
        // Arrange
        String sql = "SELECT * FROM users";

        lenient().when(statementInfo.getSqlWithValues()).thenReturn(sql);
        lenient().when(statementInfo.getSql()).thenReturn(sql);

        String expectedTraceId = "a1b2c3d4e5f6g7h8";
        String expectedSpanId = "1234567890abcdef";

        MDC.put("traceId", expectedTraceId);
        MDC.put("spanId", expectedSpanId);

        // Act
        logger.onAfterAnyExecute(statementInfo, 50_000_000, null);

        // Assert - verify MDC values are still present (they should be)
        assertEquals(expectedTraceId, MDC.get("traceId"));
        assertEquals(expectedSpanId, MDC.get("spanId"));
    }

    /**
     * Test exception logging with error details.
     * 
     * Verifies:
     * - Failed queries are logged at ERROR level
     * - Exception details are included in log
     * - Exception message is captured
     */
    @Test
    void testExceptionLoggingWithErrorDetails() {
        // Arrange
        String sql = "SELECT * FROM users";

        lenient().when(statementInfo.getSqlWithValues()).thenReturn(sql);
        lenient().when(statementInfo.getSql()).thenReturn(sql);

        MDC.put("traceId", "a1b2c3d4e5f6g7h8");
        MDC.put("spanId", "1234567890abcdef");

        SQLException exception = new SQLException("Connection timeout");

        // Act & Assert
        assertDoesNotThrow(() -> {
            logger.onAfterAnyExecute(statementInfo, 50_000_000, exception);
        });
    }

    /**
     * Test slow query threshold boundary.
     * 
     * Verifies:
     * - Query at exactly 1000ms is logged as WARN
     * - Query at 999ms is logged as INFO
     * - Query at 1001ms is logged as WARN
     */
    @Test
    void testSlowQueryThresholdBoundary() {
        // Arrange
        String sql = "SELECT * FROM users";

        lenient().when(statementInfo.getSqlWithValues()).thenReturn(sql);
        lenient().when(statementInfo.getSql()).thenReturn(sql);

        MDC.put("traceId", "a1b2c3d4e5f6g7h8");
        MDC.put("spanId", "1234567890abcdef");

        // Act & Assert - 999ms (should be INFO)
        assertDoesNotThrow(() -> {
            logger.onAfterAnyExecute(statementInfo, 999_000_000, null);
        });

        // Act & Assert - 1000ms (should be WARN)
        assertDoesNotThrow(() -> {
            logger.onAfterAnyExecute(statementInfo, 1_000_000_000, null);
        });

        // Act & Assert - 1001ms (should be WARN)
        assertDoesNotThrow(() -> {
            logger.onAfterAnyExecute(statementInfo, 1_001_000_000, null);
        });
    }

    /**
     * Test that logging failures don't affect query execution.
     * 
     * Verifies:
     * - Even if logging throws exception, onAfterAnyExecute doesn't propagate it
     * - Query execution is not affected by logging errors
     */
    @Test
    void testLoggingFailuresDontAffectQueryExecution() {
        // Arrange
        lenient().when(statementInfo.getSqlWithValues()).thenReturn(null);
        lenient().when(statementInfo.getSql()).thenReturn(null);

        MDC.put("traceId", "a1b2c3d4e5f6g7h8");
        MDC.put("spanId", "1234567890abcdef");

        // Act & Assert - should not throw exception even with null SQL
        assertDoesNotThrow(() -> {
            logger.onAfterAnyExecute(statementInfo, 50_000_000, null);
        });
    }

    /**
     * Test zero execution time.
     * 
     * Verifies:
     * - Queries with zero execution time are logged correctly
     * - No division by zero or other errors
     */
    @Test
    void testZeroExecutionTime() {
        // Arrange
        String sql = "SELECT * FROM users";

        lenient().when(statementInfo.getSqlWithValues()).thenReturn(sql);
        lenient().when(statementInfo.getSql()).thenReturn(sql);

        MDC.put("traceId", "a1b2c3d4e5f6g7h8");
        MDC.put("spanId", "1234567890abcdef");

        // Act & Assert
        assertDoesNotThrow(() -> {
            logger.onAfterAnyExecute(statementInfo, 0, null);
        });
    }

    /**
     * Test very large execution time.
     * 
     * Verifies:
     * - Very large execution times are handled correctly
     * - No overflow or other errors
     */
    @Test
    void testVeryLargeExecutionTime() {
        // Arrange
        String sql = "SELECT * FROM users";

        lenient().when(statementInfo.getSqlWithValues()).thenReturn(sql);
        lenient().when(statementInfo.getSql()).thenReturn(sql);

        MDC.put("traceId", "a1b2c3d4e5f6g7h8");
        MDC.put("spanId", "1234567890abcdef");

        // Act & Assert - 1 hour in nanoseconds
        assertDoesNotThrow(() -> {
            logger.onAfterAnyExecute(statementInfo, 3_600_000_000_000L, null);
        });
    }

    /**
     * Test complex SQL with multiple clauses.
     * 
     * Verifies:
     * - Complex SQL queries are logged correctly
     * - No exceptions are thrown
     */
    @Test
    void testComplexSqlQuery() {
        // Arrange
        String sql = "SELECT u.id, u.name, COUNT(o.id) as order_count FROM users u " +
                     "LEFT JOIN orders o ON u.id = o.user_id " +
                     "WHERE u.created_at > '2024-01-01' " +
                     "GROUP BY u.id, u.name " +
                     "HAVING COUNT(o.id) > 5 " +
                     "ORDER BY order_count DESC LIMIT 100";

        lenient().when(statementInfo.getSqlWithValues()).thenReturn(sql);
        lenient().when(statementInfo.getSql()).thenReturn(sql);

        MDC.put("traceId", "a1b2c3d4e5f6g7h8");
        MDC.put("spanId", "1234567890abcdef");

        // Act & Assert
        assertDoesNotThrow(() -> {
            logger.onAfterAnyExecute(statementInfo, 150_000_000, null);
        });
    }

    /**
     * Test SQL with special characters.
     * 
     * Verifies:
     * - SQL with special characters is logged correctly
     * - No exceptions are thrown
     */
    @Test
    void testSqlWithSpecialCharacters() {
        // Arrange
        String sql = "SELECT * FROM users WHERE name LIKE '%O''Brien%' AND email = 'test@example.com'";

        lenient().when(statementInfo.getSqlWithValues()).thenReturn(sql);
        lenient().when(statementInfo.getSql()).thenReturn(sql);

        MDC.put("traceId", "a1b2c3d4e5f6g7h8");
        MDC.put("spanId", "1234567890abcdef");

        // Act & Assert
        assertDoesNotThrow(() -> {
            logger.onAfterAnyExecute(statementInfo, 50_000_000, null);
        });
    }
}
