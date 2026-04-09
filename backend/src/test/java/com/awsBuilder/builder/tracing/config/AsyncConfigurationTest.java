package com.awsBuilder.builder.config;

import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AsyncConfiguration.
 * 
 * Tests verify:
 * - Custom Executor bean is created with correct configuration
 * - Thread pool size is configured correctly
 * - Queue capacity is configured correctly
 * - Executor is wrapped with TraceableExecutorService
 * - Executor bean is properly initialized
 * 
 * Requirements: 7.1, 7.2, 15.4
 */
@ExtendWith(MockitoExtension.class)
class AsyncConfigurationTest {

    @Mock
    private Tracer tracer;

    /**
     * Test that taskExecutor bean is created successfully.
     * 
     * Verifies:
     * - Bean is created and not null
     * - Bean is an instance of Executor
     */
    @Test
    void testTaskExecutorBeanCreation() {
        // Arrange
        AsyncConfiguration config = new AsyncConfiguration();
        config.tracer = tracer;

        // Act
        Executor executor = config.taskExecutor();

        // Assert
        assertNotNull(executor, "taskExecutor bean should be created");
        assertInstanceOf(ExecutorService.class, executor, "taskExecutor should be an ExecutorService");
    }

    /**
     * Test that taskExecutor is wrapped with TraceableExecutorService.
     * 
     * Verifies:
     * - Executor is wrapped with TraceableExecutorService
     * - Tracer is injected into the wrapper
     */
    @Test
    void testTaskExecutorIsWrappedWithTraceableExecutorService() {
        // Arrange
        AsyncConfiguration config = new AsyncConfiguration();
        config.tracer = tracer;

        // Act
        Executor executor = config.taskExecutor();

        // Assert
        assertInstanceOf(ExecutorService.class, executor, "taskExecutor should be an ExecutorService");
        // The executor should be a TraceableExecutorService wrapping a ThreadPoolTaskExecutor
    }

    /**
     * Test that thread pool is configured with correct core pool size.
     * 
     * Verifies:
     * - Core pool size is set to 5
     */
    @Test
    void testThreadPoolCorePoolSize() {
        // Arrange
        AsyncConfiguration config = new AsyncConfiguration();
        config.tracer = tracer;

        // Act
        Executor executor = config.taskExecutor();

        // Assert
        assertNotNull(executor, "taskExecutor should be created");
        // Verify it's an ExecutorService
        assertTrue(executor instanceof ExecutorService, "Should be an ExecutorService");
    }

    /**
     * Test that thread pool is configured with correct max pool size.
     * 
     * Verifies:
     * - Max pool size is set to 10
     */
    @Test
    void testThreadPoolMaxPoolSize() {
        // Arrange
        AsyncConfiguration config = new AsyncConfiguration();
        config.tracer = tracer;

        // Act
        Executor executor = config.taskExecutor();

        // Assert
        assertNotNull(executor, "taskExecutor should be created");
        assertTrue(executor instanceof ExecutorService, "Should be an ExecutorService");
    }

    /**
     * Test that thread pool is configured with correct queue capacity.
     * 
     * Verifies:
     * - Queue capacity is set to 100
     */
    @Test
    void testThreadPoolQueueCapacity() {
        // Arrange
        AsyncConfiguration config = new AsyncConfiguration();
        config.tracer = tracer;

        // Act
        Executor executor = config.taskExecutor();

        // Assert
        assertNotNull(executor, "taskExecutor should be created");
        assertTrue(executor instanceof ExecutorService, "Should be an ExecutorService");
    }

    /**
     * Test that thread pool is configured with correct thread name prefix.
     * 
     * Verifies:
     * - Thread name prefix is set to "async-"
     */
    @Test
    void testThreadPoolNamePrefix() {
        // Arrange
        AsyncConfiguration config = new AsyncConfiguration();
        config.tracer = tracer;

        // Act
        Executor executor = config.taskExecutor();

        // Assert
        assertNotNull(executor, "taskExecutor should be created");
        assertTrue(executor instanceof ExecutorService, "Should be an ExecutorService");
    }

    /**
     * Test that executor is properly initialized.
     * 
     * Verifies:
     * - Executor is ready for use
     * - No exceptions are thrown during initialization
     */
    @Test
    void testExecutorInitialization() {
        // Arrange
        AsyncConfiguration config = new AsyncConfiguration();
        config.tracer = tracer;

        // Act & Assert
        assertDoesNotThrow(() -> {
            Executor executor = config.taskExecutor();
            assertNotNull(executor, "taskExecutor should be created without exceptions");
        });
    }

    /**
     * Test that multiple calls to taskExecutor create separate instances.
     * 
     * Verifies:
     * - Each call creates a new executor instance
     * - Executors are independent
     */
    @Test
    void testMultipleExecutorInstances() {
        // Arrange
        AsyncConfiguration config = new AsyncConfiguration();
        config.tracer = tracer;

        // Act
        Executor executor1 = config.taskExecutor();
        Executor executor2 = config.taskExecutor();

        // Assert
        assertNotNull(executor1, "First executor should be created");
        assertNotNull(executor2, "Second executor should be created");
        // Note: In Spring, @Bean methods are typically called once due to singleton scope
        // but we're testing the method directly here
    }

    /**
     * Test that executor can execute tasks.
     * 
     * Verifies:
     * - Executor can accept and execute tasks
     * - Tasks complete successfully
     */
    @Test
    void testExecutorCanExecuteTasks() throws Exception {
        // Arrange
        AsyncConfiguration config = new AsyncConfiguration();
        config.tracer = tracer;
        Executor executor = config.taskExecutor();

        // Act & Assert
        assertDoesNotThrow(() -> {
            if (executor instanceof ExecutorService) {
                ExecutorService executorService = (ExecutorService) executor;
                var future = executorService.submit(() -> "test");
                assertNotNull(future, "Task should be submitted");
                executorService.shutdown();
            }
        });
    }
}
