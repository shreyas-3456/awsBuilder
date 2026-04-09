package com.awsBuilder.builder.diagram.controller;

import com.awsBuilder.builder.diagram.model.DiagramDTO;
import com.awsBuilder.builder.diagram.model.GenerateResponse;
import com.awsBuilder.builder.validation.model.ValidationResult;
import com.awsBuilder.builder.diagram.service.DiagramService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for diagram operations with structured logging and tracing.
 * 
 * This controller demonstrates transparent integration with the tracing infrastructure.
 * All log statements automatically include trace context (traceId, spanId) without
 * requiring any explicit trace handling code.
 * 
 * Example log output:
 * {
 *   "@timestamp": "2024-01-15T10:30:45.123Z",
 *   "level": "INFO",
 *   "traceId": "a1b2c3d4e5f6g7h8",
 *   "spanId": "1234567890abcdef",
 *   "logger": "com.awsBuilder.builder.diagram.controller.DiagramController",
 *   "message": "Received request to generate Terraform for 5 resources"
 * }
 * 
 * Requirements: 15.1, 15.2, 15.3, 15.4
 */
@RestController
@RequestMapping("/api/diagrams")
public class DiagramController {

    private static final Logger logger = LoggerFactory.getLogger(DiagramController.class);

    private final DiagramService diagramService;

    public DiagramController(DiagramService diagramService) {
        this.diagramService = diagramService;
    }

    /**
     * Generates both Terraform and CloudFormation code from a diagram.
     * 
     * This endpoint demonstrates:
     * - Automatic trace context propagation from HTTP request
     * - Structured logging with trace context
     * - Service layer integration with tracing
     * - Error handling with trace context
     * - Dual code generation response
     * 
     * The trace context is automatically captured by TracingMDCFilter and
     * propagated through the service layer. All logs include traceId and spanId.
     * 
     * @param diagram the diagram to generate code for
     * @return GenerateResponse containing both terraform and cloudformation code
     */
    @PostMapping("/generate")
    public ResponseEntity<GenerateResponse> generateTerraform(
            @Valid @RequestBody DiagramDTO diagram) {
        
        // Log includes trace context automatically
        logger.info("Received request to generate code for {} resources", 
                diagram.getNodes().size());
        
        try {
            GenerateResponse response = diagramService.generateCode(diagram);
            
            // Log includes trace context automatically
            logger.info("Successfully generated code - Terraform: {} chars, CloudFormation: {} chars", 
                    response.terraform().length(), response.cloudformation().length());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // Error log includes trace context and stack trace automatically
            logger.error("Failed to generate code", e);
            throw e;
        }
    }

    /**
     * Validates a diagram.
     * 
     * This endpoint demonstrates:
     * - Validation with structured logging
     * - Trace context propagation through validation layer
     * - Error details with trace context
     * 
     * @param diagram the diagram to validate
     * @return validation result
     */
    @PostMapping("/validate")
    public ResponseEntity<ValidationResult> validateDiagram(
            @Valid @RequestBody DiagramDTO diagram) {
        
        // Log includes trace context automatically
        logger.info("Validating diagram with {} nodes and {} edges", 
                diagram.getNodes().size(), diagram.getEdges().size());
        
        try {
            ValidationResult result = diagramService.validateDiagram(diagram);
            
            if (result.isValid()) {
                logger.info("Diagram validation passed");
                return ResponseEntity.ok(result);
            } else {
                logger.warn("Diagram validation failed with {} errors", 
                        result.getErrors().size());
                throw new com.awsBuilder.builder.exception.ValidationException(
                        "Diagram validation failed", result.getErrors());
            }
            
        } catch (com.awsBuilder.builder.exception.ValidationException ve) {
            throw ve;
        } catch (Exception e) {
            // Error log includes trace context and stack trace automatically
            logger.error("Diagram validation failed with exception", e);
            throw e;
        }
    }
}
