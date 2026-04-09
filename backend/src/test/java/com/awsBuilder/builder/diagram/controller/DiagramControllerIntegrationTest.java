package com.awsBuilder.builder.diagram.controller;

import com.awsBuilder.builder.diagram.service.DiagramService;
import com.awsBuilder.builder.diagram.model.GenerateResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DiagramController.class)
class DiagramControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DiagramService diagramService;

    @MockitoBean
    private io.micrometer.tracing.Tracer tracer;

    @MockitoBean
    private io.micrometer.tracing.Span span;

    @MockitoBean
    private io.micrometer.tracing.TraceContext traceContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /generate with valid diagram returns 200 with Terraform and CloudFormation")
    void generateWithValidDiagram() throws Exception {
        GenerateResponse response = new GenerateResponse(
            "provider \"aws\" { region = \"us-east-1\" }",
            "AWSTemplateFormatVersion: '2010-09-09'\nResources: {}"
        );
        when(diagramService.generateCode(any())).thenReturn(response);

        String requestBody = """
            {
                "nodes": [
                    {"id": "vpc1", "type": "VPC", "displayName": "My VPC", "properties": {"cidr_block": "10.0.0.0/16"}}
                ],
                "edges": [],
                "region": "us-east-1"
            }
            """;

        mockMvc.perform(post("/api/diagrams/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.terraform").exists())
            .andExpect(jsonPath("$.terraform").value("provider \"aws\" { region = \"us-east-1\" }"))
            .andExpect(jsonPath("$.cloudformation").exists())
            .andExpect(jsonPath("$.cloudformation").value("AWSTemplateFormatVersion: '2010-09-09'\nResources: {}"));
    }

    @Test
    @DisplayName("POST /generate with empty nodes returns 400 validation error")
    void generateWithEmptyNodesReturns400() throws Exception {
        String requestBody = """
            {
                "nodes": [],
                "edges": []
            }
            """;

        mockMvc.perform(post("/api/diagrams/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /generate with null nodes returns 400")
    void generateWithNullNodesReturns400() throws Exception {
        String requestBody = """
            {
                "edges": []
            }
            """;

        mockMvc.perform(post("/api/diagrams/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /generate with malformed JSON returns 400")
    void generateWithMalformedJsonReturns400() throws Exception {
        mockMvc.perform(post("/api/diagrams/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /generate with blank node id returns 400")
    void generateWithBlankNodeIdReturns400() throws Exception {
        String requestBody = """
            {
                "nodes": [
                    {"id": "", "type": "VPC", "properties": {}}
                ],
                "edges": []
            }
            """;

        mockMvc.perform(post("/api/diagrams/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /generate with blank node type returns 400")
    void generateWithBlankNodeTypeReturns400() throws Exception {
        String requestBody = """
            {
                "nodes": [
                    {"id": "vpc1", "type": "", "properties": {}}
                ],
                "edges": []
            }
            """;

        mockMvc.perform(post("/api/diagrams/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /validate endpoint returns validation result")
    void validateEndpointWorks() throws Exception {
        com.awsBuilder.builder.validation.model.ValidationResult validResult =
            new com.awsBuilder.builder.validation.model.ValidationResult(true, java.util.List.of());
        when(diagramService.validateDiagram(any())).thenReturn(validResult);

        String requestBody = """
            {
                "nodes": [
                    {"id": "vpc1", "type": "VPC", "displayName": "My VPC", "properties": {}}
                ],
                "edges": [],
                "region": "us-east-1"
            }
            """;

        mockMvc.perform(post("/api/diagrams/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.valid").value(true));
    }
}
