package com.awsBuilder.builder.diagram.service;

import com.awsBuilder.builder.validation.engine.DependencyResolver;
import com.awsBuilder.builder.validation.engine.ValidationEngine;
import com.awsBuilder.builder.exception.ValidationException;
import com.awsBuilder.builder.diagram.model.DiagramDTO;
import com.awsBuilder.builder.diagram.model.GenerateResponse;
import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.ResourceGraph;
import com.awsBuilder.builder.validation.model.ValidationResult;
import com.awsBuilder.builder.terraform.service.TerraformGenerator;
import com.awsBuilder.builder.cloudformation.service.CloudFormationGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service for diagram operations with structured logging and tracing.
 * 
 * This service demonstrates:
 * - Structured logging with automatic trace context inclusion
 * - Multi-layer trace propagation (controller → service → repository)
 * - Async operations with trace context propagation
 * - Error handling with trace context
 * 
 * All log statements automatically include trace context (traceId, spanId)
 * without requiring explicit trace handling code.
 * 
 * Example log output:
 * {
 *   "@timestamp": "2024-01-15T10:30:45.150Z",
 *   "level": "INFO",
 *   "traceId": "a1b2c3d4e5f6g7h8",
 *   "spanId": "1234567890abcdef",
 *   "logger": "com.awsBuilder.builder.diagram.service.DiagramService",
 *   "message": "Generating Terraform code for 5 resources"
 * }
 * 
 * Async Operations:
 * - Async methods use @Async annotation for non-blocking execution
 * - Trace context is automatically propagated to async threads
 * - Async logs include original traceId with child spanId
 * - MDC is automatically cleaned up in async threads
 * 
 * Requirements: 15.1, 15.2, 15.3, 15.4, 7.1, 7.2, 7.3, 7.4, 7.5
 */
@Service
public class DiagramService {
    
    private static final Logger logger = LoggerFactory.getLogger(DiagramService.class);
    
    private final ValidationEngine validationEngine;
    private final DependencyResolver dependencyResolver;
    private final TerraformGenerator terraformGenerator;
    private final CloudFormationGenerator cloudFormationGenerator;
    
    public DiagramService(ValidationEngine validationEngine,
                         DependencyResolver dependencyResolver,
                         TerraformGenerator terraformGenerator,
                         CloudFormationGenerator cloudFormationGenerator) {
        this.validationEngine = validationEngine;
        this.dependencyResolver = dependencyResolver;
        this.terraformGenerator = terraformGenerator;
        this.cloudFormationGenerator = cloudFormationGenerator;
    }
    
    /**
     * Generates both Terraform and CloudFormation code from a diagram.
     * 
     * This method demonstrates:
     * - Structured logging with trace context
     * - Multi-layer trace propagation
     * - Error handling with trace context
     * - Dual code generation from a single validated graph
     * 
     * All logs include trace context automatically:
     * - Validation logs include traceId and spanId
     * - Dependency resolution logs include traceId and spanId
     * - Code generation logs include traceId and spanId
     * 
     * @param diagram the diagram to generate code for
     * @return GenerateResponse containing both terraform and cloudformation strings
     * @throws ValidationException if diagram validation fails
     */
    public GenerateResponse generateCode(DiagramDTO diagram) {
        // Step 1: Validate diagram
        // Log includes trace context automatically
        logger.debug("Starting code generation for diagram with {} nodes", 
                diagram.getNodes().size());
        
        ValidationResult validationResult = validationEngine.validate(diagram);
        if (!validationResult.isValid()) {
            // Error log includes trace context automatically
            logger.error("Diagram validation failed with {} errors", 
                    validationResult.getErrors().size());
            throw new ValidationException("Diagram validation failed", validationResult.getErrors());
        }
        
        // Step 2: Build dependency graph
        // Log includes trace context automatically
        logger.debug("Building dependency graph");
        ResourceGraph graph = dependencyResolver.buildGraph(diagram);
        
        // Step 3: Perform topological sort
        // Log includes trace context automatically
        logger.debug("Performing topological sort");
        List<NodeDTO> orderedNodes = dependencyResolver.topologicalSort(graph);
        
        // Step 4: Generate both Terraform and CloudFormation code
        // Log includes trace context automatically
        logger.info("Generating code for {} resources", orderedNodes.size());
        String terraformOutput = terraformGenerator.generate(orderedNodes, graph, diagram.getRegion());
        String cloudformationOutput = cloudFormationGenerator.generate(orderedNodes, graph, diagram.getRegion());
        
        // Log includes trace context automatically
        logger.debug("Code generation completed - Terraform: {} chars, CloudFormation: {} chars", 
                terraformOutput.length(), cloudformationOutput.length());
        
        return new GenerateResponse(terraformOutput, cloudformationOutput);
    }
    
    /**
     * Validates a diagram.
     * 
     * This method demonstrates:
     * - Structured logging with trace context
     * - Error handling with trace context
     * 
     * @param diagram the diagram to validate
     * @return validation result
     */
    public ValidationResult validateDiagram(DiagramDTO diagram) {
        // Log includes trace context automatically
        logger.debug("Validating diagram with {} nodes and {} edges", 
                diagram.getNodes().size(), diagram.getEdges().size());
        
        try {
            ValidationResult result = validationEngine.validate(diagram);
            
            if (result.isValid()) {
                logger.debug("Diagram validation passed");
            } else {
                logger.warn("Diagram validation failed with {} errors", 
                        result.getErrors().size());
            }
            
            return result;
            
        } catch (Exception e) {
            // Error log includes trace context and stack trace automatically
            logger.error("Diagram validation failed with exception", e);
            throw e;
        }
    }
    
    /**
     * Asynchronously generates code from a diagram.
     * 
     * This method demonstrates:
     * - Async operations with @Async annotation
     * - Trace context propagation to async threads
     * - Async logs include original traceId with child spanId
     * - MDC cleanup in async threads
     * 
     * The @Async annotation causes this method to execute in a separate thread
     * from the TraceableExecutorService. The trace context is automatically
     * propagated to the async thread, so all logs include the original traceId.
     * 
     * Example usage:
     * <pre>
     * CompletableFuture<GenerateResponse> future = diagramService.generateCodeAsync(diagram);
     * GenerateResponse response = future.get(); // Wait for async operation to complete
     * </pre>
     * 
     * @param diagram the diagram to generate code for
     * @return CompletableFuture containing GenerateResponse with both terraform and cloudformation
     */
    @Async
    public CompletableFuture<GenerateResponse> generateCodeAsync(DiagramDTO diagram) {
        try {
            // Log includes original traceId with child spanId automatically
            logger.info("Starting async code generation for {} resources", 
                    diagram.getNodes().size());
            
            GenerateResponse response = generateCode(diagram);
            
            // Log includes original traceId with child spanId automatically
            logger.info("Async code generation completed");
            
            return CompletableFuture.completedFuture(response);
            
        } catch (Exception e) {
            // Error log includes trace context and stack trace automatically
            logger.error("Async code generation failed", e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * Asynchronously validates a diagram.
     * 
     * This method demonstrates:
     * - Async validation with @Async annotation
     * - Trace context propagation to async threads
     * - Async logs include original traceId with child spanId
     * 
     * @param diagram the diagram to validate
     * @return CompletableFuture containing validation result
     */
    @Async
    public CompletableFuture<ValidationResult> validateDiagramAsync(DiagramDTO diagram) {
        try {
            // Log includes original traceId with child spanId automatically
            logger.debug("Starting async diagram validation");
            
            ValidationResult result = validateDiagram(diagram);
            
            // Log includes original traceId with child spanId automatically
            logger.debug("Async diagram validation completed");
            
            return CompletableFuture.completedFuture(result);
            
        } catch (Exception e) {
            // Error log includes trace context and stack trace automatically
            logger.error("Async diagram validation failed", e);
            return CompletableFuture.failedFuture(e);
        }
    }
}
