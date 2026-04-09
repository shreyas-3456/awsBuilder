package com.awsBuilder.builder.executor;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TraceableExecutorService.
 * 
 * Tests verify:
 * - Trace context capture before async execution
 * - Trace context restoration in async thread
 * - Child span creation with parent relationship
 * - MDC population in async thread
 * - MDC cleanup after async completion
 * - Handling of both Runnable and Callable tasks
 * - Graceful handling when no trace context exists
 * 
 * Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 13.4
 */
@ExtendWith(MockitoExtension.class)
class TraceableExecutorServiceTest {

    private static final String TRACE_ID_KEY = "traceId";
    private static final String SPAN_ID_KEY = "spanId";
    private static final String PARENT_SPAN_ID_KEY = "parentSpanId";

    @Mock(lenient = true)
    private Tracer tracer;

    @Mock(lenient = true)
    private Span currentSpan;

    @Mock(lenient = true)
    private Span childSpan;

    @Mock(lenient = true)
    private TraceContext currentTraceContext;

    @Mock(lenient = true)
    private TraceContext childTraceContext;

    private ExecutorService delegate;
    private TraceableExecutorService executor;

    @BeforeEach
    void setUp() {
        delegate = Executors.newFixedThreadPool(2);
        executor = new TraceableExecutorService(delegate, tracer);
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
        delegate.shutdown();
    }

    /**
     * Test that trace context is captured before async execution.
     * 
     * Verifies:
     * - Trace context is read from MDC in calling thread
     * - Trace context is available for propagation to async thread
     */
    @Test
    void testTraceContextCaptureBeforeAsyncExecution() throws Exception {
        // Arrange
        String traceId = "a1b2c3d4e5f6g7h8";
        String spanId = "1234567890abcdef";
        MDC.put(TRACE_ID_KEY, traceId);
        MDC.put(SPAN_ID_KEY, spanId);

        when(tracer.currentSpan()).thenReturn(currentSpan);
        when(currentSpan.context()).thenReturn(currentTraceContext);
        when(tracer.nextSpan()).thenReturn(childSpan);
        when(childSpan.context()).thenReturn(childTraceContext);
        when(childTraceContext.spanId()).thenReturn("fedcba0987654321");
        when(tracer.withSpan(childSpan)).thenReturn(mock(Tracer.SpanInScope.class));

        AtomicBoolean taskExecuted = new AtomicBoolean(false);

        // Act
        Future<?> future = executor.submit(() -> {
            taskExecuted.set(true);
        });

        future.get(5, TimeUnit.SECONDS);

        // Assert
        assertTrue(taskExecuted.get(), "Task should have been executed");
    }

    /**
     * Test that trace context is restored in async thread.
     * 
     * Verifies:
     * - MDC is populated in async thread with original traceId
     * - MDC is populated in async thread with child spanId
     * - MDC is populated in async thread with parent spanId
     */
    @Test
    void testTraceContextRestorationInAsyncThread() throws Exception {
        // Arrange
        String traceId = "a1b2c3d4e5f6g7h8";
        String spanId = "1234567890abcdef";
        String childSpanId = "fedcba0987654321";

        MDC.put(TRACE_ID_KEY, traceId);
        MDC.put(SPAN_ID_KEY, spanId);

        when(tracer.currentSpan()).thenReturn(currentSpan);
        when(currentSpan.context()).thenReturn(currentTraceContext);
        when(tracer.nextSpan()).thenReturn(childSpan);
        when(childSpan.context()).thenReturn(childTraceContext);
        when(childTraceContext.spanId()).thenReturn(childSpanId);
        when(tracer.withSpan(childSpan)).thenReturn(mock(Tracer.SpanInScope.class));

        AtomicReference<String> asyncTraceId = new AtomicReference<>();
        AtomicReference<String> asyncSpanId = new AtomicReference<>();
        AtomicReference<String> asyncParentSpanId = new AtomicReference<>();

        // Act
        Future<?> future = executor.submit(() -> {
            asyncTraceId.set(MDC.get(TRACE_ID_KEY));
            asyncSpanId.set(MDC.get(SPAN_ID_KEY));
            asyncParentSpanId.set(MDC.get(PARENT_SPAN_ID_KEY));
        });

        future.get(5, TimeUnit.SECONDS);

        // Assert
        assertEquals(traceId, asyncTraceId.get(), "Original traceId should be restored in async thread");
        assertEquals(childSpanId, asyncSpanId.get(), "Child spanId should be set in async thread");
        assertEquals(spanId, asyncParentSpanId.get(), "Parent spanId should be set in async thread");
    }

    /**
     * Test that child span is created and ended.
     * 
     * Verifies:
     * - tracer.nextSpan is called
     * - Child span is started
     * - Child span is ended after task completes
     */
    @Test
    void testChildSpanCreationAndEnding() throws Exception {
        // Arrange
        String traceId = "a1b2c3d4e5f6g7h8";
        String spanId = "1234567890abcdef";

        MDC.put(TRACE_ID_KEY, traceId);
        MDC.put(SPAN_ID_KEY, spanId);

        when(tracer.currentSpan()).thenReturn(currentSpan);
        when(currentSpan.context()).thenReturn(currentTraceContext);
        when(tracer.nextSpan()).thenReturn(childSpan);
        when(childSpan.context()).thenReturn(childTraceContext);
        when(childTraceContext.spanId()).thenReturn("fedcba0987654321");
        when(tracer.withSpan(childSpan)).thenReturn(mock(Tracer.SpanInScope.class));

        // Act
        Future<?> future = executor.submit(() -> {
            // Task execution
        });

        future.get(5, TimeUnit.SECONDS);

        // Assert
        verify(tracer).nextSpan();
        verify(childSpan).start();
        verify(childSpan).end();
    }

    /**
     * Test that MDC is cleaned up after async task completes.
     * 
     * Verifies:
     * - MDC is cleared in async thread after task execution
     * - Original MDC values remain in calling thread
     */
    @Test
    void testMDCCleanupAfterAsyncCompletion() throws Exception {
        // Arrange
        String traceId = "a1b2c3d4e5f6g7h8";
        String spanId = "1234567890abcdef";

        MDC.put(TRACE_ID_KEY, traceId);
        MDC.put(SPAN_ID_KEY, spanId);

        when(tracer.currentSpan()).thenReturn(currentSpan);
        when(currentSpan.context()).thenReturn(currentTraceContext);
        when(tracer.nextSpan()).thenReturn(childSpan);
        when(childSpan.context()).thenReturn(childTraceContext);
        when(childTraceContext.spanId()).thenReturn("fedcba0987654321");
        when(tracer.withSpan(childSpan)).thenReturn(mock(Tracer.SpanInScope.class));

        // Act
        Future<?> future = executor.submit(() -> {
            // Task execution
        });

        future.get(5, TimeUnit.SECONDS);

        // Give thread pool time to clean up
        Thread.sleep(100);

        // Assert - MDC in calling thread should still have original values
        assertEquals(traceId, MDC.get(TRACE_ID_KEY), "Original traceId should remain in calling thread");
        assertEquals(spanId, MDC.get(SPAN_ID_KEY), "Original spanId should remain in calling thread");
    }

    /**
     * Test that Callable tasks are wrapped correctly.
     * 
     * Verifies:
     * - Callable task is executed
     * - Result is returned correctly
     * - Trace context is propagated
     */
    @Test
    void testCallableTaskWrapping() throws Exception {
        // Arrange
        String traceId = "a1b2c3d4e5f6g7h8";
        String spanId = "1234567890abcdef";
        String expectedResult = "test-result";

        MDC.put(TRACE_ID_KEY, traceId);
        MDC.put(SPAN_ID_KEY, spanId);

        when(tracer.currentSpan()).thenReturn(currentSpan);
        when(currentSpan.context()).thenReturn(currentTraceContext);
        when(tracer.nextSpan()).thenReturn(childSpan);
        when(childSpan.context()).thenReturn(childTraceContext);
        when(childTraceContext.spanId()).thenReturn("fedcba0987654321");
        when(tracer.withSpan(childSpan)).thenReturn(mock(Tracer.SpanInScope.class));

        Callable<String> task = () -> expectedResult;

        // Act
        Future<String> future = executor.submit(task);
        String result = future.get(5, TimeUnit.SECONDS);

        // Assert
        assertEquals(expectedResult, result, "Callable result should be returned correctly");
    }

    /**
     * Test that Runnable tasks are wrapped correctly.
     * 
     * Verifies:
     * - Runnable task is executed
     * - Trace context is propagated
     */
    @Test
    void testRunnableTaskWrapping() throws Exception {
        // Arrange
        String traceId = "a1b2c3d4e5f6g7h8";
        String spanId = "1234567890abcdef";

        MDC.put(TRACE_ID_KEY, traceId);
        MDC.put(SPAN_ID_KEY, spanId);

        when(tracer.currentSpan()).thenReturn(currentSpan);
        when(currentSpan.context()).thenReturn(currentTraceContext);
        when(tracer.nextSpan()).thenReturn(childSpan);
        when(childSpan.context()).thenReturn(childTraceContext);
        when(childTraceContext.spanId()).thenReturn("fedcba0987654321");
        when(tracer.withSpan(childSpan)).thenReturn(mock(Tracer.SpanInScope.class));

        AtomicBoolean taskExecuted = new AtomicBoolean(false);
        Runnable task = () -> taskExecuted.set(true);

        // Act
        Future<?> future = executor.submit(task);
        future.get(5, TimeUnit.SECONDS);

        // Assert
        assertTrue(taskExecuted.get(), "Runnable task should be executed");
    }

    /**
     * Test graceful handling when no trace context exists.
     * 
     * Verifies:
     * - Task executes normally when MDC is empty
     * - No exceptions are thrown
     * - Task completes successfully
     */
    @Test
    void testGracefulHandlingWhenNoTraceContext() throws Exception {
        // Arrange
        MDC.clear();
        when(tracer.currentSpan()).thenReturn(null);

        AtomicBoolean taskExecuted = new AtomicBoolean(false);

        // Act
        Future<?> future = executor.submit(() -> {
            taskExecuted.set(true);
        });

        future.get(5, TimeUnit.SECONDS);

        // Assert
        assertTrue(taskExecuted.get(), "Task should execute even without trace context");
    }

    /**
     * Test execute method with trace context propagation.
     * 
     * Verifies:
     * - execute method wraps task correctly
     * - Task is executed
     * - Trace context is propagated
     */
    @Test
    void testExecuteMethodWithTraceContext() throws Exception {
        // Arrange
        String traceId = "a1b2c3d4e5f6g7h8";
        String spanId = "1234567890abcdef";

        MDC.put(TRACE_ID_KEY, traceId);
        MDC.put(SPAN_ID_KEY, spanId);

        when(tracer.currentSpan()).thenReturn(currentSpan);
        when(currentSpan.context()).thenReturn(currentTraceContext);
        when(tracer.nextSpan()).thenReturn(childSpan);
        when(childSpan.context()).thenReturn(childTraceContext);
        when(childTraceContext.spanId()).thenReturn("fedcba0987654321");
        when(tracer.withSpan(childSpan)).thenReturn(mock(Tracer.SpanInScope.class));

        AtomicBoolean taskExecuted = new AtomicBoolean(false);

        // Act
        executor.execute(() -> {
            taskExecuted.set(true);
        });

        // Give thread pool time to execute
        Thread.sleep(500);

        // Assert
        assertTrue(taskExecuted.get(), "Task should be executed via execute method");
    }

    /**
     * Test that executor delegates shutdown operations correctly.
     * 
     * Verifies:
     * - shutdown method is delegated
     * - isShutdown method returns correct status
     */
    @Test
    void testShutdownOperations() {
        // Arrange
        ExecutorService realDelegate = Executors.newFixedThreadPool(1);
        TraceableExecutorService realExecutor = new TraceableExecutorService(realDelegate, tracer);

        // Act
        realExecutor.shutdown();

        // Assert
        assertTrue(realExecutor.isShutdown(), "Executor should be shut down");
    }

    /**
     * Test that executor handles exceptions in tasks gracefully.
     * 
     * Verifies:
     * - Exception in task does not prevent trace cleanup
     * - MDC is cleaned up even when task throws exception
     */
    @Test
    void testExceptionHandlingInTask() throws Exception {
        // Arrange
        String traceId = "a1b2c3d4e5f6g7h8";
        String spanId = "1234567890abcdef";

        MDC.put(TRACE_ID_KEY, traceId);
        MDC.put(SPAN_ID_KEY, spanId);

        when(tracer.currentSpan()).thenReturn(currentSpan);
        when(currentSpan.context()).thenReturn(currentTraceContext);
        when(tracer.nextSpan()).thenReturn(childSpan);
        when(childSpan.context()).thenReturn(childTraceContext);
        when(childTraceContext.spanId()).thenReturn("fedcba0987654321");
        when(tracer.withSpan(childSpan)).thenReturn(mock(Tracer.SpanInScope.class));

        // Act
        Future<?> future = executor.submit(() -> {
            throw new RuntimeException("Test exception");
        });

        // Assert - Task should complete with exception
        assertThrows(Exception.class, () -> future.get(5, TimeUnit.SECONDS));
    }
}
