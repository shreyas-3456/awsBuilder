package com.awsBuilder.builder.config;

import com.awsBuilder.builder.executor.TraceableExecutorService;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * Configuration class for Spring async support with distributed tracing.
 * 
 * This configuration enables async method execution with automatic trace context propagation.
 * Methods annotated with @Async will execute in a thread pool, with trace context from the
 * calling thread automatically propagated to the async thread.
 * 
 * The custom Executor bean wraps the thread pool with TraceableExecutorService to ensure
 * that trace context is captured before task submission and restored in the async thread.
 * 
 * Thread Pool Configuration:
 * - Core pool size: 5 threads
 * - Max pool size: 10 threads
 * - Queue capacity: 100 tasks
 * - Thread name prefix: "async-"
 * - Wait for tasks to complete on shutdown
 * 
 * Example usage:
 * <pre>
 * @Service
 * public class MyService {
 *     @Async
 *     public void asyncOperation() {
 *         logger.info("Async operation"); // Log includes original traceId
 *     }
 * }
 * </pre>
 * 
 * Requirements: 7.1, 7.2, 15.4
 */
@Configuration
@EnableAsync
public class AsyncConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(AsyncConfiguration.class);

    @Autowired
    Tracer tracer;

    /**
     * Creates a custom Executor bean for async method execution with trace context propagation.
     * 
     * This bean wraps a ThreadPoolTaskExecutor with TraceableExecutorService to ensure
     * that trace context is automatically propagated to async tasks.
     * 
     * Thread Pool Configuration:
     * - Core pool size: 5 threads (minimum threads kept alive)
     * - Max pool size: 10 threads (maximum concurrent tasks)
     * - Queue capacity: 100 tasks (tasks waiting for execution)
     * - Thread name prefix: "async-" (for easy identification in logs)
     * - Wait for tasks to complete on shutdown: true
     * - Await termination seconds: 60 seconds
     * 
     * Preconditions:
     * - Tracer bean is initialized and available
     * 
     * Postconditions:
     * - Returns an Executor that propagates trace context to async tasks
     * - Thread pool is configured and ready for use
     * - All @Async methods will use this executor
     * 
     * @return a custom Executor with trace context propagation
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        logger.debug("Initializing async task executor with trace context propagation");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();

        logger.debug("ThreadPoolTaskExecutor initialized with corePoolSize=5, maxPoolSize=10, queueCapacity=100");

        // Wrap the executor with TraceableExecutorService for trace context propagation
        ExecutorService traceableExecutor = new TraceableExecutorService(
                executor.getThreadPoolExecutor(),
                tracer
        );

        logger.debug("Executor wrapped with TraceableExecutorService for trace context propagation");

        return traceableExecutor;
    }
}
