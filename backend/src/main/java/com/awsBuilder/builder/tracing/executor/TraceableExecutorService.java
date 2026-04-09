package com.awsBuilder.builder.executor;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Wrapper for ExecutorService that captures and propagates trace context to async operations.
 * 
 * This wrapper ensures that when tasks are submitted to the executor, the current trace context
 * is captured and restored in the async thread before the task executes. This enables distributed
 * tracing across asynchronous operations, allowing logs from async tasks to be correlated with
 * the originating request.
 * 
 * Features:
 * - Captures trace context before task submission
 * - Creates child span with original traceId and new spanId in async thread
 * - Populates MDC in async thread with trace context
 * - Cleans up trace context and MDC after async task completes
 * - Handles both Runnable and Callable tasks
 * - Supports all ExecutorService methods (execute, submit, invokeAll, invokeAny)
 * 
 * Thread Safety:
 * - Trace context is captured in the calling thread
 * - Trace context is restored in the async thread
 * - MDC is thread-local, so each thread maintains separate context
 * - Safe for use in concurrent request processing
 * 
 * Example usage:
 * <pre>
 * ExecutorService executor = new TraceableExecutorService(
 *     Executors.newFixedThreadPool(10),
 *     tracer
 * );
 * 
 * // Trace context is automatically propagated to async task
 * executor.submit(() -> {
 *     logger.info("Async operation"); // Log includes original traceId
 * });
 * </pre>
 * 
 * Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 13.4
 */
public class TraceableExecutorService implements ExecutorService {

    private static final Logger logger = LoggerFactory.getLogger(TraceableExecutorService.class);

    private static final String TRACE_ID_KEY = "traceId";
    private static final String SPAN_ID_KEY = "spanId";
    private static final String PARENT_SPAN_ID_KEY = "parentSpanId";

    private final ExecutorService delegate;
    private final Tracer tracer;

    /**
     * Creates a new TraceableExecutorService wrapper.
     * 
     * @param delegate the underlying ExecutorService to wrap
     * @param tracer the Micrometer Tracer for creating child spans
     */
    public TraceableExecutorService(ExecutorService delegate, Tracer tracer) {
        this.delegate = delegate;
        this.tracer = tracer;
    }

    /**
     * Executes a runnable task with trace context propagation.
     * 
     * Captures the current trace context and ensures it is restored in the async thread
     * before the task executes.
     * 
     * @param command the runnable task to execute
     */
    @Override
    public void execute(Runnable command) {
        Runnable wrappedCommand = wrapRunnable(command);
        delegate.execute(wrappedCommand);
    }

    /**
     * Submits a runnable task with trace context propagation.
     * 
     * @param task the runnable task to submit
     * @return a Future representing the task
     */
    @Override
    public Future<?> submit(Runnable task) {
        Runnable wrappedTask = wrapRunnable(task);
        return delegate.submit(wrappedTask);
    }

    /**
     * Submits a callable task with trace context propagation.
     * 
     * @param task the callable task to submit
     * @return a Future representing the task
     */
    @Override
    public <T> Future<T> submit(Callable<T> task) {
        Callable<T> wrappedTask = wrapCallable(task);
        return delegate.submit(wrappedTask);
    }

    /**
     * Submits a runnable task with a result value and trace context propagation.
     * 
     * @param task the runnable task to submit
     * @param result the result value to return
     * @return a Future representing the task
     */
    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        Runnable wrappedTask = wrapRunnable(task);
        return delegate.submit(wrappedTask, result);
    }

    /**
     * Executes all callable tasks with trace context propagation.
     * 
     * @param tasks the callable tasks to execute
     * @return a list of Futures representing the tasks
     */
    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
            throws InterruptedException {
        List<Callable<T>> wrappedTasks = tasks.stream()
                .map(this::wrapCallable)
                .collect(Collectors.toList());
        return delegate.invokeAll(wrappedTasks);
    }

    /**
     * Executes all callable tasks with trace context propagation and timeout.
     * 
     * @param tasks the callable tasks to execute
     * @param timeout the timeout duration
     * @param unit the timeout unit
     * @return a list of Futures representing the tasks
     */
    @Override
    public <T> List<Future<T>> invokeAll(
            Collection<? extends Callable<T>> tasks,
            long timeout,
            TimeUnit unit) throws InterruptedException {
        List<Callable<T>> wrappedTasks = tasks.stream()
                .map(this::wrapCallable)
                .collect(Collectors.toList());
        return delegate.invokeAll(wrappedTasks, timeout, unit);
    }

    /**
     * Executes any callable task with trace context propagation.
     * 
     * @param tasks the callable tasks to execute
     * @return the result of the first completed task
     */
    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
            throws InterruptedException, ExecutionException {
        List<Callable<T>> wrappedTasks = tasks.stream()
                .map(this::wrapCallable)
                .collect(Collectors.toList());
        return delegate.invokeAny(wrappedTasks);
    }

    /**
     * Executes any callable task with trace context propagation and timeout.
     * 
     * @param tasks the callable tasks to execute
     * @param timeout the timeout duration
     * @param unit the timeout unit
     * @return the result of the first completed task
     */
    @Override
    public <T> T invokeAny(
            Collection<? extends Callable<T>> tasks,
            long timeout,
            TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        List<Callable<T>> wrappedTasks = tasks.stream()
                .map(this::wrapCallable)
                .collect(Collectors.toList());
        return delegate.invokeAny(wrappedTasks, timeout, unit);
    }

    /**
     * Initiates an orderly shutdown of the executor.
     */
    @Override
    public void shutdown() {
        delegate.shutdown();
    }

    /**
     * Attempts to stop all actively executing tasks.
     * 
     * @return a list of tasks that never commenced execution
     */
    @Override
    public List<Runnable> shutdownNow() {
        return delegate.shutdownNow();
    }

    /**
     * Returns whether the executor has been shut down.
     * 
     * @return true if the executor has been shut down
     */
    @Override
    public boolean isShutdown() {
        return delegate.isShutdown();
    }

    /**
     * Returns whether all tasks have completed following shutdown.
     * 
     * @return true if all tasks have completed
     */
    @Override
    public boolean isTerminated() {
        return delegate.isTerminated();
    }

    /**
     * Blocks until all tasks have completed execution after a shutdown request.
     * 
     * @param timeout the maximum time to wait
     * @param unit the time unit
     * @return true if the executor terminated, false if the timeout elapsed
     */
    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.awaitTermination(timeout, unit);
    }

    /**
     * Wraps a Runnable to capture and propagate trace context.
     * 
     * Captures the current trace context before the task is submitted, then restores
     * it in the async thread before executing the task.
     * 
     * @param task the runnable task to wrap
     * @return a wrapped runnable that propagates trace context
     */
    private Runnable wrapRunnable(Runnable task) {
        // Capture trace context in the calling thread
        String traceId = MDC.get(TRACE_ID_KEY);
        String spanId = MDC.get(SPAN_ID_KEY);
        Span currentSpan = tracer.currentSpan();

        return () -> {
            // Restore trace context in the async thread
            try {
                if (traceId != null && spanId != null && currentSpan != null) {
                    // Create child span with original traceId and new spanId
                    Span childSpan = tracer.nextSpan();
                    childSpan.start();

                    try (Tracer.SpanInScope scope = tracer.withSpan(childSpan)) {
                        // Populate MDC in async thread
                        MDC.put(TRACE_ID_KEY, traceId);
                        MDC.put(SPAN_ID_KEY, childSpan.context().spanId());
                        MDC.put(PARENT_SPAN_ID_KEY, spanId);

                        logger.debug("Trace context restored in async thread - traceId={}, spanId={}",
                                traceId, childSpan.context().spanId());

                        // Execute the task
                        task.run();
                    } finally {
                        // Clean up trace context and MDC in async thread
                        MDC.remove(TRACE_ID_KEY);
                        MDC.remove(SPAN_ID_KEY);
                        MDC.remove(PARENT_SPAN_ID_KEY);
                        childSpan.end();
                    }
                } else {
                    // No trace context to propagate, execute task normally
                    logger.debug("No trace context to propagate, executing task without tracing");
                    task.run();
                }
            } catch (Exception e) {
                logger.warn("Error during trace context propagation in async task", e);
                // Continue execution even if tracing fails
                task.run();
            }
        };
    }

    /**
     * Wraps a Callable to capture and propagate trace context.
     * 
     * Captures the current trace context before the task is submitted, then restores
     * it in the async thread before executing the task.
     * 
     * @param task the callable task to wrap
     * @return a wrapped callable that propagates trace context
     */
    private <T> Callable<T> wrapCallable(Callable<T> task) {
        // Capture trace context in the calling thread
        String traceId = MDC.get(TRACE_ID_KEY);
        String spanId = MDC.get(SPAN_ID_KEY);
        Span currentSpan = tracer.currentSpan();

        return () -> {
            // Restore trace context in the async thread
            try {
                if (traceId != null && spanId != null && currentSpan != null) {
                    // Create child span with original traceId and new spanId
                    Span childSpan = tracer.nextSpan();
                    childSpan.start();

                    try (Tracer.SpanInScope scope = tracer.withSpan(childSpan)) {
                        // Populate MDC in async thread
                        MDC.put(TRACE_ID_KEY, traceId);
                        MDC.put(SPAN_ID_KEY, childSpan.context().spanId());
                        MDC.put(PARENT_SPAN_ID_KEY, spanId);

                        logger.debug("Trace context restored in async thread - traceId={}, spanId={}",
                                traceId, childSpan.context().spanId());

                        // Execute the task
                        return task.call();
                    } finally {
                        // Clean up trace context and MDC in async thread
                        MDC.remove(TRACE_ID_KEY);
                        MDC.remove(SPAN_ID_KEY);
                        MDC.remove(PARENT_SPAN_ID_KEY);
                        childSpan.end();
                    }
                } else {
                    // No trace context to propagate, execute task normally
                    logger.debug("No trace context to propagate, executing task without tracing");
                    return task.call();
                }
            } catch (Exception e) {
                logger.warn("Error during trace context propagation in async task", e);
                // Continue execution even if tracing fails
                return task.call();
            }
        };
    }
}
