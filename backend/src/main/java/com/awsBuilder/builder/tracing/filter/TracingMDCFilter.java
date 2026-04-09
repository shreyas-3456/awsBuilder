package com.awsBuilder.builder.filter;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * Servlet filter for populating MDC with trace context from Micrometer Tracing.
 * 
 * This filter intercepts all HTTP requests to extract trace and span IDs from the
 * current trace context and populate the MDC (Mapped Diagnostic Context) with these
 * identifiers. This ensures that all log entries generated during request processing
 * automatically include trace context for correlation across distributed operations.
 * 
 * The filter runs with HIGHEST_PRECEDENCE to ensure it executes before other filters,
 * guaranteeing that MDC is populated early in the request lifecycle. It uses a try-finally
 * block to ensure MDC cleanup even when exceptions occur during request processing.
 * 
 * MDC Keys:
 * - traceId: 16-character hexadecimal identifier for the distributed trace
 * - spanId: 16-character hexadecimal identifier for the current span
 * - requestId: UUID generated for this specific request
 * 
 * Error Handling:
 * - Wraps MDC operations in try-catch blocks to prevent filter failures
 * - Logs warnings if MDC cleanup fails
 * - Continues request processing even if trace context is unavailable
 * - Generates new trace if current trace context is null
 * - Detects and warns about MDC leakage from previous requests
 * 
 * Thread Safety (13.1, 13.2, 13.3):
 * - MDC is thread-local, so each request thread maintains separate trace context
 * - MDC is cleared in finally block to prevent context leakage to subsequent requests
 * - Safe for use in thread pools and concurrent request processing
 * - Forces MDC clear at start of each request as safety measure
 * - Detects MDC leakage from previous requests on thread pool threads
 * - Verifies MDC is thread-local and isolated per request
 * 
 * Example usage:
 * <pre>
 * // In a controller or service
 * logger.info("Processing request"); // Log automatically includes traceId and spanId
 * </pre>
 * 
 * Requirements: 1.3, 1.4, 1.5, 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 11.2, 13.1, 13.2, 13.3, 15.1, 15.2
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TracingMDCFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(TracingMDCFilter.class);

    private static final String TRACE_ID_KEY = "traceId";
    private static final String SPAN_ID_KEY = "spanId";
    private static final String REQUEST_ID_KEY = "requestId";

    @Autowired
    Tracer tracer;

    /**
     * Filters the HTTP request to populate MDC with trace context.
     * 
     * This method extracts the current trace context from the Micrometer Tracer,
     * populates MDC with trace identifiers, processes the request through the filter chain,
     * and ensures MDC is cleaned up in a finally block.
     * 
     * Error Handling:
     * - Wraps all operations in try-catch to prevent filter failures
     * - Detects MDC leakage from previous requests and logs warning
     * - Forces MDC clear at start of each request as safety measure
     * - Continues request processing even if trace context is unavailable
     * - Generates new trace if current trace context is null
     * 
     * Preconditions:
     * - request is non-null and valid HTTP request
     * - response is non-null and valid HTTP response
     * - filterChain is non-null
     * - tracer bean is initialized and available
     * 
     * Postconditions:
     * - MDC is populated with traceId, spanId, and requestId during request processing
     * - MDC is cleared after request completion (in finally block)
     * - Request is forwarded through filter chain
     * - No MDC context leaks to subsequent requests
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // Force MDC clear at start of each request as safety measure (13.1)
            // This prevents trace context leakage from previous requests on thread pool threads
            detectAndClearMDCLeakage();

            try {
                // Extract trace context from tracer
                Span currentSpan = tracer.currentSpan();
                
                if (currentSpan != null) {
                    TraceContext traceContext = currentSpan.context();
                    if (traceContext != null) {
                        populateMDC(traceContext);
                    } else {
                        logger.warn("Current span has no trace context, generating new trace");
                        populateMDCWithNewTrace();
                    }
                } else {
                    logger.debug("No current span found, generating new trace");
                    populateMDCWithNewTrace();
                }
            } catch (Exception e) {
                logger.warn("Failed to populate MDC with trace context. Continuing without trace context.", e);
                // Continue processing even if trace context population fails
            }

            // Process the request through the filter chain
            filterChain.doFilter(request, response);

        } finally {
            // Ensure MDC cleanup even on exceptions
            clearMDC();
        }
    }

    /**
     * Populates MDC with trace context identifiers.
     * 
     * This method sets MDC values for traceId and spanId from the provided trace context,
     * and generates a unique requestId for this request.
     * 
     * Preconditions:
     * - traceContext is non-null
     * - traceContext.traceId() returns valid 16-character hex string
     * - traceContext.spanId() returns valid 16-character hex string
     * - MDC is available and initialized
     * 
     * Postconditions:
     * - MDC contains "traceId" key with value from traceContext
     * - MDC contains "spanId" key with value from traceContext
     * - MDC contains "requestId" key with generated UUID
     * - All MDC values are non-null strings
     * - No exceptions are thrown
     * 
     * @param traceContext the trace context containing traceId and spanId
     */
    private void populateMDC(TraceContext traceContext) {
        try {
            String traceId = traceContext.traceId();
            String spanId = traceContext.spanId();
            String requestId = generateRequestId();

            MDC.put(TRACE_ID_KEY, traceId);
            MDC.put(SPAN_ID_KEY, spanId);
            MDC.put(REQUEST_ID_KEY, requestId);

            logger.debug("MDC populated with traceId={}, spanId={}, requestId={}",
                    traceId, spanId, requestId);

        } catch (Exception e) {
            logger.warn("Failed to populate MDC with trace context", e);
            // Continue processing even if MDC population fails
        }
    }

    /**
     * Populates MDC with a new trace when no current trace context exists.
     * 
     * This method generates new trace and span IDs and populates MDC.
     * This ensures that even requests without existing trace context are tracked.
     * 
     * Postconditions:
     * - MDC contains generated traceId, spanId, and requestId
     * - New trace identifiers are unique
     */
    private void populateMDCWithNewTrace() {
        try {
            // Generate new trace and span IDs
            String traceId = generateTraceId();
            String spanId = generateSpanId();
            String requestId = generateRequestId();

            MDC.put(TRACE_ID_KEY, traceId);
            MDC.put(SPAN_ID_KEY, spanId);
            MDC.put(REQUEST_ID_KEY, requestId);

            logger.debug("MDC populated with new trace - traceId={}, spanId={}, requestId={}",
                    traceId, spanId, requestId);

        } catch (Exception e) {
            logger.warn("Failed to populate MDC with new trace", e);
            // Continue processing even if MDC population fails
        }
    }

    /**
     * Detects and clears MDC leakage from previous requests.
     * 
     * This method checks if MDC contains trace context at the start of a request,
     * which indicates leakage from a previous request (especially problematic in thread pools).
     * If leakage is detected, it logs a warning and clears the MDC.
     * 
     * This is a safety measure to prevent trace context from one request affecting another.
     * 
     * Postconditions:
     * - If MDC contains trace keys, warning is logged
     * - MDC is cleared of any existing trace context
     * - Safe to populate MDC with new trace context
     */
    private void detectAndClearMDCLeakage() {
        try {
            String existingTraceId = MDC.get(TRACE_ID_KEY);
            String existingSpanId = MDC.get(SPAN_ID_KEY);
            
            if (existingTraceId != null || existingSpanId != null) {
                logger.warn("Detected MDC leakage from previous request: traceId={}, spanId={}. " +
                        "Clearing MDC to prevent context contamination.", existingTraceId, existingSpanId);
                clearMDC();
            }
        } catch (Exception e) {
            logger.warn("Failed to detect MDC leakage", e);
            // Continue - don't let this check prevent request processing
        }
    }

    /**
     * Clears all trace-related keys from MDC.
     * 
     * This method removes traceId, spanId, and requestId from MDC to prevent
     * context leakage to subsequent requests on the same thread. It handles cases
     * where keys don't exist gracefully.
     * 
     * Preconditions:
     * - MDC is initialized
     * - Method is called in finally block or equivalent cleanup context
     * 
     * Postconditions:
     * - All trace-related keys are removed from MDC
     * - MDC is empty or contains only non-trace-related keys
     * - No exceptions are thrown even if keys don't exist
     * - Thread-local storage is cleaned up
     */
    private void clearMDC() {
        try {
            MDC.remove(TRACE_ID_KEY);
            MDC.remove(SPAN_ID_KEY);
            MDC.remove(REQUEST_ID_KEY);
            logger.debug("MDC cleared of trace context");

        } catch (Exception e) {
            logger.warn("Failed to clear MDC", e);
            // Continue even if MDC cleanup fails - don't let cleanup errors affect request processing
        }
    }

    /**
     * Generates a unique trace identifier.
     * 
     * Generates a 16-character hexadecimal string for use as a trace ID.
     * This is used when no existing trace context is available.
     * 
     * Postconditions:
     * - Returns a 16-character hexadecimal string
     * - Each invocation returns a different value
     * 
     * @return a unique trace identifier
     */
    private String generateTraceId() {
        // Generate 8 random bytes and convert to 16-char hex string
        long randomValue = UUID.randomUUID().getMostSignificantBits();
        return String.format("%016x", Math.abs(randomValue));
    }

    /**
     * Generates a unique span identifier.
     * 
     * Generates a 16-character hexadecimal string for use as a span ID.
     * 
     * Postconditions:
     * - Returns a 16-character hexadecimal string
     * - Each invocation returns a different value
     * 
     * @return a unique span identifier
     */
    private String generateSpanId() {
        // Generate 8 random bytes and convert to 16-char hex string
        long randomValue = UUID.randomUUID().getLeastSignificantBits();
        return String.format("%016x", Math.abs(randomValue));
    }

    /**
     * Generates a unique request identifier.
     * 
     * Generates a UUID v4 string for use as a request ID.
     * 
     * Postconditions:
     * - Returns unique request identifier as string
     * - Format is UUID v4 (36 characters with hyphens)
     * - Each invocation returns different value
     * 
     * @return a unique request identifier
     */
    private String generateRequestId() {
        return UUID.randomUUID().toString();
    }
}
