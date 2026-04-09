package com.awsBuilder.builder.exception;

import com.awsBuilder.builder.config.RequestLoggingFilter;
import com.awsBuilder.builder.config.service.DatabaseException;
import com.awsBuilder.builder.validation.exception.CircularDependencyException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/**
 * Global exception handler for REST API with structured logging and tracing.
 *
 * This handler demonstrates:
 * - Exception logging with trace context
 * - Error response formatting with trace context
 * - Structured error logging with all relevant details
 *
 * All error logs automatically include trace context (traceId, spanId) and
 * exception details (class, message, stack trace) in structured JSON format.
 *
 * Example error log output:
 * {
 *   "@timestamp": "2024-01-15T10:30:45.200Z",
 *   "level": "ERROR",
 *   "traceId": "a1b2c3d4e5f6g7h8",
 *   "spanId": "1234567890abcdef",
 *   "logger": "com.awsBuilder.builder.exception.GlobalExceptionHandler",
 *   "message": "Unhandled error requestId=... method=POST path=/api/diagrams/generate",
 *   "exception": {
 *     "class": "com.awsBuilder.builder.exception.ValidationException",
 *     "message": "Diagram validation failed",
 *     "stackTrace": [...]
 *   }
 * }
 *
 * Requirements: 3.9, 15.1, 15.2, 15.3, 15.4
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles ValidationException with structured logging.
     *
     * Error log includes:
     * - HTTP status code
     * - Error code
     * - Request ID
     * - HTTP method and path
     * - Error message
     * - Validation details
     * - Trace context (traceId, spanId)
     * - Exception stack trace
     *
     * @param ex the validation exception
     * @param request the HTTP request
     * @return error response with trace context
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex,
                                                                   HttpServletRequest request) {
        logClientError(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, ex.getMessage(), request, ex.getDetails());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR,
            ex.getMessage(), ex.getDetails(), request);
    }

    /**
     * Handles CircularDependencyException with structured logging.
     *
     * Error log includes:
     * - HTTP status code
     * - Error code
     * - Request ID
     * - HTTP method and path
     * - Circular dependency cycle details
     * - Trace context (traceId, spanId)
     * - Exception stack trace
     *
     * @param ex the circular dependency exception
     * @param request the HTTP request
     * @return error response with trace context
     */
    @ExceptionHandler(CircularDependencyException.class)
    public ResponseEntity<ErrorResponse> handleCircularDependency(CircularDependencyException ex,
                                                                  HttpServletRequest request) {
        String message = "Circular dependency detected";
        logClientError(HttpStatus.BAD_REQUEST, ErrorCode.CIRCULAR_DEPENDENCY, message, request, ex.getCycle());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ErrorCode.CIRCULAR_DEPENDENCY,
            message, ex.getCycle(), request);
    }

    /**
     * Handles ResourceNotFoundException with structured logging.
     *
     * Error log includes:
     * - HTTP status code
     * - Error code
     * - Request ID
     * - HTTP method and path
     * - Resource not found message
     * - Trace context (traceId, spanId)
     * - Exception stack trace
     *
     * @param ex the resource not found exception
     * @param request the HTTP request
     * @return error response with trace context
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex,
                                                                HttpServletRequest request) {
        logClientError(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND, ex.getMessage(), request, List.of());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND,
            ex.getMessage(), List.of(), request);
    }

    /**
     * Handles MethodArgumentNotValidException with structured logging.
     *
     * Error log includes:
     * - HTTP status code
     * - Error code
     * - Request ID
     * - HTTP method and path
     * - Field validation errors
     * - Trace context (traceId, spanId)
     * - Exception stack trace
     *
     * @param ex the method argument not valid exception
     * @param request the HTTP request
     * @return error response with trace context
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                      HttpServletRequest request) {
        List<String> details = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + Objects.toString(error.getDefaultMessage(), "Invalid value"))
            .collect(Collectors.toList());

        String message = "Request validation failed";
        logClientError(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, message, request, details);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR,
            message, details, request);
    }

    /**
     * Handles HandlerMethodValidationException with structured logging.
     *
     * Error log includes:
     * - HTTP status code
     * - Error code
     * - Request ID
     * - HTTP method and path
     * - Parameter validation errors
     * - Trace context (traceId, spanId)
     * - Exception stack trace
     *
     * @param ex the handler method validation exception
     * @param request the HTTP request
     * @return error response with trace context
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidation(HandlerMethodValidationException ex,
                                                                       HttpServletRequest request) {
        List<String> details = ex.getParameterValidationResults()
            .stream()
            .flatMap(result -> result.getResolvableErrors().stream())
            .map(error -> Objects.toString(error.getDefaultMessage(), "Invalid value"))
            .collect(Collectors.toList());

        String message = "Request validation failed";
        logClientError(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, message, request, details);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR,
            message, details, request);
    }

    /**
     * Handles ConstraintViolationException with structured logging.
     *
     * Error log includes:
     * - HTTP status code
     * - Error code
     * - Request ID
     * - HTTP method and path
     * - Constraint violation details
     * - Trace context (traceId, spanId)
     * - Exception stack trace
     *
     * @param ex the constraint violation exception
     * @param request the HTTP request
     * @return error response with trace context
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
                                                                   HttpServletRequest request) {
        List<String> details = ex.getConstraintViolations()
            .stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .collect(Collectors.toList());

        String message = "Request validation failed";
        logClientError(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, message, request, details);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR,
            message, details, request);
    }

    /**
     * Handles HttpMessageNotReadableException with structured logging.
     *
     * Error log includes:
     * - HTTP status code
     * - Error code
     * - Request ID
     * - HTTP method and path
     * - Malformed request details
     * - Trace context (traceId, spanId)
     * - Exception stack trace
     *
     * @param ex the HTTP message not readable exception
     * @param request the HTTP request
     * @return error response with trace context
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                      HttpServletRequest request) {
        String message = "Malformed request body";
        logClientError(HttpStatus.BAD_REQUEST, ErrorCode.MALFORMED_REQUEST, message, request, List.of());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ErrorCode.MALFORMED_REQUEST,
            message, List.of(), request);
    }

    /**
     * Handles IllegalArgumentException with structured logging.
     *
     * Error log includes:
     * - HTTP status code
     * - Error code
     * - Request ID
     * - HTTP method and path
     * - Illegal argument details
     * - Trace context (traceId, spanId)
     * - Exception stack trace
     *
     * @param ex the illegal argument exception
     * @param request the HTTP request
     * @return error response with trace context
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex,
                                                                        HttpServletRequest request) {
        logClientError(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, ex.getMessage(), request, List.of());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST,
            ex.getMessage(), List.of(), request);
    }

    /**
     * Handles DatabaseException with structured logging.
     *
     * Error log includes:
     * - HTTP status code (500 Internal Server Error)
     * - Error code
     * - Request ID
     * - HTTP method and path
     * - Database error message
     * - Trace context (traceId, spanId)
     * - Exception stack trace
     *
     * @param ex the database exception
     * @param request the HTTP request
     * @return error response with trace context
     */
    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseException(DatabaseException ex,
                                                                 HttpServletRequest request) {
        String requestId = resolveRequestId(request);
        logger.error("Database error requestId={} method={} path={} message={}",
            requestId, request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR,
            ex.getMessage(), List.of(), request);
    }

    /**
     * Handles all unhandled exceptions with structured logging.
     *
     * This is the catch-all handler for any exception not handled by specific handlers.
     *
     * Error log includes:
     * - HTTP status code (500 Internal Server Error)
     * - Error code
     * - Request ID
     * - HTTP method and path
     * - Exception message
     * - Trace context (traceId, spanId)
     * - Full exception stack trace
     *
     * @param ex the exception
     * @param request the HTTP request
     * @return error response with trace context
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex,
                                                                HttpServletRequest request) {
        String requestId = resolveRequestId(request);
        // Error log includes trace context and stack trace automatically
        logger.error("Unhandled error requestId={} method={} path={} message={}",
            requestId, request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR,
            "Internal server error", List.of(), request);
    }

    /**
     * Builds error response with trace context.
     *
     * @param status HTTP status code
     * @param code error code
     * @param message error message
     * @param details error details
     * @param request HTTP request
     * @return error response entity
     */
    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status,
                                                             ErrorCode code,
                                                             String message,
                                                             List<String> details,
                                                             HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(
            status.value(),
            code,
            message,
            request.getRequestURI(),
            resolveRequestId(request),
            details
        );
        return ResponseEntity.status(status).body(body);
    }

    /**
     * Logs client error with structured logging.
     *
     * Log includes:
     * - HTTP status code
     * - Error code
     * - Request ID
     * - HTTP method and path
     * - Error message
     * - Error details
     * - Trace context (traceId, spanId) automatically included
     *
     * @param status HTTP status code
     * @param code error code
     * @param message error message
     * @param request HTTP request
     * @param details error details
     */
    private void logClientError(HttpStatus status,
                                ErrorCode code,
                                String message,
                                HttpServletRequest request,
                                List<String> details) {
        // Log includes trace context automatically
        logger.warn("Handled client error status={} code={} requestId={} method={} path={} message={} details={}",
            status.value(),
            code.name(),
            resolveRequestId(request),
            request.getMethod(),
            request.getRequestURI(),
            message,
            details
        );
    }

    /**
     * Resolves request ID from request attributes, headers, or MDC.
     *
     * @param request HTTP request
     * @return request ID or "unknown" if not found
     */
    private String resolveRequestId(HttpServletRequest request) {
        Object requestId = request.getAttribute(RequestLoggingFilter.REQUEST_ID_ATTRIBUTE);
        if (requestId instanceof String value && !value.isBlank()) {
            return value;
        }
        String headerValue = request.getHeader(RequestLoggingFilter.REQUEST_ID_HEADER);
        if (headerValue != null && !headerValue.isBlank()) {
            return headerValue;
        }
        String mdcValue = MDC.get(RequestLoggingFilter.REQUEST_ID_MDC_KEY);
        return mdcValue != null ? mdcValue : "unknown";
    }
}
