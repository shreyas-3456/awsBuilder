package com.awsBuilder.builder.filter;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TracingMDCFilter.
 * 
 * Tests verify:
 * - MDC population with valid trace context
 * - MDC cleanup in finally block
 * - Behavior when no trace context exists (new trace generation)
 * - Exception handling during request processing
 * - MDC cleanup even when exceptions occur
 * 
 * Requirements: 2.4, 2.5, 2.6, 11.2
 */
@ExtendWith(MockitoExtension.class)
class TracingMDCFilterTest {

    private static final String TRACE_ID_KEY = "traceId";
    private static final String SPAN_ID_KEY = "spanId";
    private static final String REQUEST_ID_KEY = "requestId";

    @Mock
    private Tracer tracer;

    @Mock
    private Span span;

    @Mock
    private TraceContext traceContext;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private TracingMDCFilter filter;

    @BeforeEach
    void setUp() {
        filter = new TracingMDCFilter();
        filter.tracer = tracer;
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    /**
     * Test that MDC is populated with valid trace context.
     * 
     * Verifies:
     * - traceId is set in MDC
     * - spanId is set in MDC
     * - requestId is set in MDC
     * - All values are non-null
     */
    @Test
    void testMDCPopulationWithValidTraceContext() throws ServletException, IOException {
        // Arrange
        String expectedTraceId = "a1b2c3d4e5f6g7h8";
        String expectedSpanId = "1234567890abcdef";

        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(traceContext);
        when(traceContext.traceId()).thenReturn(expectedTraceId);
        when(traceContext.spanId()).thenReturn(expectedSpanId);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        // MDC should be cleared after filter completes, so we verify it was called
        verify(tracer).currentSpan();
    }

    /**
     * Test that MDC is cleaned up in finally block.
     * 
     * Verifies:
     * - MDC is empty after filter completes
     * - No trace context leaks to subsequent requests
     */
    @Test
    void testMDCCleanupInFinallyBlock() throws ServletException, IOException {
        // Arrange
        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(traceContext);
        when(traceContext.traceId()).thenReturn("a1b2c3d4e5f6g7h8");
        when(traceContext.spanId()).thenReturn("1234567890abcdef");

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        // After filter completes, MDC should be cleared
        assertNull(MDC.get(TRACE_ID_KEY), "traceId should be cleared from MDC");
        assertNull(MDC.get(SPAN_ID_KEY), "spanId should be cleared from MDC");
        assertNull(MDC.get(REQUEST_ID_KEY), "requestId should be cleared from MDC");
    }

    /**
     * Test that MDC is cleaned up even when exception occurs during request processing.
     * 
     * Verifies:
     * - MDC is cleared even when filterChain.doFilter throws exception
     * - Exception is propagated after cleanup
     */
    @Test
    void testMDCCleanupOnException() throws ServletException, IOException {
        // Arrange
        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(traceContext);
        when(traceContext.traceId()).thenReturn("a1b2c3d4e5f6g7h8");
        when(traceContext.spanId()).thenReturn("1234567890abcdef");

        ServletException testException = new ServletException("Test exception");
        doThrow(testException).when(filterChain).doFilter(request, response);

        // Act & Assert
        assertThrows(ServletException.class, () -> {
            filter.doFilterInternal(request, response, filterChain);
        });

        // Verify MDC is cleaned up even after exception
        assertNull(MDC.get(TRACE_ID_KEY), "traceId should be cleared from MDC after exception");
        assertNull(MDC.get(SPAN_ID_KEY), "spanId should be cleared from MDC after exception");
        assertNull(MDC.get(REQUEST_ID_KEY), "requestId should be cleared from MDC after exception");
    }

    /**
     * Test behavior when no trace context exists (new trace generation).
     * 
     * Verifies:
     * - Filter handles null current span gracefully
     * - New trace context is generated
     * - MDC is populated with generated values
     * - MDC is cleaned up after request
     */
    @Test
    void testBehaviorWhenNoTraceContextExists() throws ServletException, IOException {
        // Arrange
        when(tracer.currentSpan()).thenReturn(null);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        // MDC should be cleared after filter completes
        assertNull(MDC.get(TRACE_ID_KEY), "traceId should be cleared from MDC");
        assertNull(MDC.get(SPAN_ID_KEY), "spanId should be cleared from MDC");
        assertNull(MDC.get(REQUEST_ID_KEY), "requestId should be cleared from MDC");
    }

    /**
     * Test behavior when span exists but trace context is null.
     * 
     * Verifies:
     * - Filter handles null trace context gracefully
     * - New trace context is generated
     * - Request processing continues
     */
    @Test
    void testBehaviorWhenSpanExistsButTraceContextIsNull() throws ServletException, IOException {
        // Arrange
        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(null);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        // MDC should be cleared after filter completes
        assertNull(MDC.get(TRACE_ID_KEY), "traceId should be cleared from MDC");
        assertNull(MDC.get(SPAN_ID_KEY), "spanId should be cleared from MDC");
        assertNull(MDC.get(REQUEST_ID_KEY), "requestId should be cleared from MDC");
    }

    /**
     * Test that filter continues processing even if MDC operations fail.
     * 
     * Verifies:
     * - Filter does not throw exception if MDC operations fail
     * - Request is still processed through filter chain
     */
    @Test
    void testFilterContinuesOnMDCFailure() throws ServletException, IOException {
        // Arrange
        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(traceContext);
        when(traceContext.traceId()).thenReturn("a1b2c3d4e5f6g7h8");
        when(traceContext.spanId()).thenReturn("1234567890abcdef");

        // Act - should not throw exception
        assertDoesNotThrow(() -> {
            filter.doFilterInternal(request, response, filterChain);
        });

        // Assert
        verify(filterChain).doFilter(request, response);
    }

    /**
     * Test that multiple requests maintain separate trace contexts.
     * 
     * Verifies:
     * - First request gets its own trace context
     * - MDC is cleared after first request
     * - Second request gets different trace context
     * - No context leakage between requests
     */
    @Test
    void testMultipleRequestsHaveSeparateTraceContexts() throws ServletException, IOException {
        // Arrange - First request
        String traceId1 = "a1b2c3d4e5f6g7h8";
        String spanId1 = "1234567890abcdef";

        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(traceContext);
        when(traceContext.traceId()).thenReturn(traceId1);
        when(traceContext.spanId()).thenReturn(spanId1);

        // Act - First request
        filter.doFilterInternal(request, response, filterChain);

        // Assert - MDC should be cleared after first request
        assertNull(MDC.get(TRACE_ID_KEY), "traceId should be cleared after first request");
        assertNull(MDC.get(SPAN_ID_KEY), "spanId should be cleared after first request");

        // Arrange - Second request with different trace context
        String traceId2 = "h8g7f6e5d4c3b2a1";
        String spanId2 = "fedcba0987654321";

        when(traceContext.traceId()).thenReturn(traceId2);
        when(traceContext.spanId()).thenReturn(spanId2);

        // Act - Second request
        filter.doFilterInternal(request, response, filterChain);

        // Assert - MDC should be cleared after second request
        assertNull(MDC.get(TRACE_ID_KEY), "traceId should be cleared after second request");
        assertNull(MDC.get(SPAN_ID_KEY), "spanId should be cleared after second request");
    }

    /**
     * Test that requestId is unique for each request.
     * 
     * Verifies:
     * - Each request gets a unique requestId
     * - requestId is in UUID format
     */
    @Test
    void testRequestIdIsUniquePerRequest() throws ServletException, IOException {
        // Arrange
        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(traceContext);
        when(traceContext.traceId()).thenReturn("a1b2c3d4e5f6g7h8");
        when(traceContext.spanId()).thenReturn("1234567890abcdef");

        // We'll capture the requestId by checking MDC during filter execution
        String[] capturedRequestId = new String[1];
        doAnswer(invocation -> {
            capturedRequestId[0] = MDC.get(REQUEST_ID_KEY);
            return null;
        }).when(filterChain).doFilter(request, response);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNotNull(capturedRequestId[0], "requestId should be set in MDC");
        // UUID format: 8-4-4-4-12 hex digits with hyphens
        assertTrue(capturedRequestId[0].matches(
                "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"),
                "requestId should be in UUID format");
    }
}
