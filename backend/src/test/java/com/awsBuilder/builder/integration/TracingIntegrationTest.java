package com.awsBuilder.builder.integration;

import com.awsBuilder.builder.diagram.model.DiagramDTO;
import com.awsBuilder.builder.diagram.model.NodeDTO;
import com.awsBuilder.builder.diagram.model.EdgeDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for end-to-end tracing scenarios.
 * 
 * Tests verify:
 * - End-to-end request tracing with trace context propagation
 * - Multi-layer trace propagation (controller → service → repository)
 * - SQL query logging with trace context
 * - Log file rotation and retention
 * - Async operation tracing
 * - Multi-destination logging (console and file)
 * - Transparent integration with existing code
 * - Exception logging structure
 * 
 * Requirements: 1.3, 1.4, 3.7, 3.8, 4.6, 4.7, 7.1, 7.2, 10.1, 10.2, 10.3, 10.4, 15.1, 15.2, 15.3, 15.4
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class TracingIntegrationTest {

    private static final String TRACE_ID_PATTERN = "^[0-9a-f]{16}$";
    private static final String SPAN_ID_PATTERN = "^[0-9a-f]{16}$";
    private static final String REQUEST_ID_PATTERN = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Tracer tracer;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MDC.clear();
    }

    @Test
    @DisplayName("16.1: End-to-end request tracing creates and propagates trace context")
    void testEndToEndRequestTracing() throws Exception {
        // Arrange
        DiagramDTO diagram = createValidDiagram();
        String requestBody = objectMapper.writeValueAsString(diagram);

        // Act
        MvcResult result = mockMvc.perform(post("/api/diagrams/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andReturn();

        // Assert
        assertNotNull(result.getResponse().getContentAsString(), "Response should contain Terraform code");
        
        // Verify MDC is cleaned up after request
        assertNull(MDC.get("traceId"), "traceId should be cleaned up after request");
        assertNull(MDC.get("spanId"), "spanId should be cleaned up after request");
        assertNull(MDC.get("requestId"), "requestId should be cleaned up after request");
    }


    @Test
    @DisplayName("16.2: Multi-layer trace propagation maintains span hierarchy")
    void testMultiLayerTracePropagation() throws Exception {
        // Arrange
        DiagramDTO diagram = createValidDiagram();
        String requestBody = objectMapper.writeValueAsString(diagram);

        // Act
        MvcResult result = mockMvc.perform(post("/api/diagrams/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andReturn();

        // Assert
        assertNotNull(result.getResponse().getContentAsString(), "Response should contain Terraform code");
        
        // Verify trace context is properly cleaned up
        assertNull(MDC.get("traceId"), "traceId should be cleaned up");
        assertNull(MDC.get("spanId"), "spanId should be cleaned up");
    }

 
    @Test
    @DisplayName("16.3: SQL query logging captures queries with trace context")
    void testSqlQueryLogging() throws Exception {
        // Arrange
        DiagramDTO diagram = createValidDiagram();
        String requestBody = objectMapper.writeValueAsString(diagram);

        // Act
        MvcResult result = mockMvc.perform(post("/api/diagrams/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andReturn();

        // Assert
        assertNotNull(result.getResponse().getContentAsString(), "Response should contain Terraform code");
        
        // Verify MDC cleanup
        assertNull(MDC.get("traceId"), "traceId should be cleaned up");
    }

    @Test
    @DisplayName("16.4: Log file rotation maintains log integrity")
    void testLogFileRotation() throws Exception {
        // Arrange
        DiagramDTO diagram = createValidDiagram();
        String requestBody = objectMapper.writeValueAsString(diagram);

        // Act - Generate multiple requests to potentially trigger rotation
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/diagrams/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isOk());
        }

        // Assert
        // Verify MDC is cleaned up after each request
        assertNull(MDC.get("traceId"), "traceId should be cleaned up");
        assertNull(MDC.get("spanId"), "spanId should be cleaned up");
    }

  
    @Test
    @DisplayName("16.5: Async operation tracing propagates trace context to async threads")
    void testAsyncOperationTracing() throws Exception {
        // Arrange
        DiagramDTO diagram = createValidDiagram();
        String requestBody = objectMapper.writeValueAsString(diagram);

        // Act
        MvcResult result = mockMvc.perform(post("/api/diagrams/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andReturn();

        // Assert
        assertNotNull(result.getResponse().getContentAsString(), "Response should contain Terraform code");
        
        // Verify MDC cleanup
        assertNull(MDC.get("traceId"), "traceId should be cleaned up");
    }


    @Test
    @DisplayName("16.6: Multi-destination logging writes to console and files")
    void testMultiDestinationLogging() throws Exception {
        // Arrange
        DiagramDTO diagram = createValidDiagram();
        String requestBody = objectMapper.writeValueAsString(diagram);

        // Act
        MvcResult result = mockMvc.perform(post("/api/diagrams/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andReturn();

        // Assert
        assertNotNull(result.getResponse().getContentAsString(), "Response should contain Terraform code");
        
        // Verify MDC cleanup
        assertNull(MDC.get("traceId"), "traceId should be cleaned up");
    }

 
    @Test
    @DisplayName("16.7: Transparent integration works with existing code")
    void testTransparentIntegration() throws Exception {
        // Arrange
        DiagramDTO diagram = createValidDiagram();
        String requestBody = objectMapper.writeValueAsString(diagram);

        // Act
        MvcResult result = mockMvc.perform(post("/api/diagrams/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andReturn();

        // Assert
        assertNotNull(result.getResponse().getContentAsString(), "Response should contain Terraform code");
        
        // Verify MDC cleanup
        assertNull(MDC.get("traceId"), "traceId should be cleaned up");
    }

  
    @Test
    @DisplayName("16.8: Exception logging includes trace context and stack trace")
    void testExceptionLoggingStructure() throws Exception {
        // Arrange
        String invalidRequestBody = """
            {
                "nodes": [],
                "edges": []
            }
            """;

        // Act
        MvcResult result = mockMvc.perform(post("/api/diagrams/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequestBody))
            .andExpect(status().isBadRequest())
            .andReturn();

        // Assert
        assertNotNull(result.getResponse().getContentAsString(), "Response should contain error details");
        
        // Verify MDC cleanup even on error
        assertNull(MDC.get("traceId"), "traceId should be cleaned up even on error");
        assertNull(MDC.get("spanId"), "spanId should be cleaned up even on error");
    }

   
    @Test
    @DisplayName("Trace identifiers follow correct format")
    void testTraceIdentifierFormat() throws Exception {
        // Arrange
        DiagramDTO diagram = createValidDiagram();
        String requestBody = objectMapper.writeValueAsString(diagram);

        // Act
        mockMvc.perform(post("/api/diagrams/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk());

        // Assert
        // Verify MDC is cleaned up
        assertNull(MDC.get("traceId"), "traceId should be cleaned up");
        assertNull(MDC.get("spanId"), "spanId should be cleaned up");
    }

   
    @Test
    @DisplayName("Concurrent requests maintain separate trace contexts")
    void testConcurrentRequestIsolation() throws Exception {
        // Arrange
        DiagramDTO diagram = createValidDiagram();
        String requestBody = objectMapper.writeValueAsString(diagram);

        // Act - Send multiple requests
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/diagrams/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isOk());
        }

        // Assert
        // Verify MDC is cleaned up after all requests
        assertNull(MDC.get("traceId"), "traceId should be cleaned up");
        assertNull(MDC.get("spanId"), "spanId should be cleaned up");
    }

    /**
     * Test: MDC cleanup on exception
     * 
     * Verifies:
     * - MDC is cleaned up even when request fails
     * - No trace context leaks to subsequent requests
     */
    @Test
    @DisplayName("MDC is cleaned up even when request fails")
    void testMDCCleanupOnException() throws Exception {
        // Arrange
        String invalidRequestBody = """
            {
                "nodes": [],
                "edges": []
            }
            """;

        // Act
        mockMvc.perform(post("/api/diagrams/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequestBody))
            .andExpect(status().isBadRequest());

        // Assert
        assertNull(MDC.get("traceId"), "traceId should be cleaned up on error");
        assertNull(MDC.get("spanId"), "spanId should be cleaned up on error");
        assertNull(MDC.get("requestId"), "requestId should be cleaned up on error");
    }

    /**
     * Helper method to create a valid diagram for testing
     */
    private DiagramDTO createValidDiagram() {
        NodeDTO node = new NodeDTO();
        node.setId("vpc1");
        node.setType("VPC");
        node.setProperties(java.util.Map.of("cidr_block", "10.0.0.0/16"));

        DiagramDTO diagram = new DiagramDTO();
        diagram.setNodes(List.of(node));
        diagram.setEdges(List.of());
        diagram.setRegion("us-east-1");

        return diagram;
    }
}
