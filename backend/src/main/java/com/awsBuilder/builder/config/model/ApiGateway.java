package com.awsBuilder.builder.config.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;

/**
 * JPA entity for AWS API Gateway APIs.
 * Maps to aws_api_gateways table.
 */
@Entity
@Table(name = "aws_api_gateways")
public class ApiGateway {
    @Id
    @Column(name = "api_id")
    private String apiId;

    @Column(name = "api_name")
    private String apiName;

    @Column(name = "region")
    private String region;

    @Column(name = "protocol_type")
    private String protocolType;

    @Column(name = "endpoint_type")
    private String endpointType;

    @Column(name = "description")
    private String description;

    @Column(name = "use_case")
    private String useCase;

    @Column(name = "status")
    private String status;

    @Column(name = "api_key_source")
    private String apiKeySource;

    @Column(name = "route_selection_expression")
    private String routeSelectionExpression;

    @Column(name = "auth_types", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> authTypes;

    @Column(name = "cors_configuration", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> corsConfiguration;

    @Column(name = "stages", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> stages;

    @Column(name = "tags", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> tags;

    @Column(name = "source")
    private String source;

    @Column(name = "collected_at")
    private Instant collectedAt;

    // Getters and setters
    public String getApiId() { return apiId; }
    public void setApiId(String apiId) { this.apiId = apiId; }

    public String getApiName() { return apiName; }
    public void setApiName(String apiName) { this.apiName = apiName; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getProtocolType() { return protocolType; }
    public void setProtocolType(String protocolType) { this.protocolType = protocolType; }

    public String getEndpointType() { return endpointType; }
    public void setEndpointType(String endpointType) { this.endpointType = endpointType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUseCase() { return useCase; }
    public void setUseCase(String useCase) { this.useCase = useCase; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getApiKeySource() { return apiKeySource; }
    public void setApiKeySource(String apiKeySource) { this.apiKeySource = apiKeySource; }

    public String getRouteSelectionExpression() { return routeSelectionExpression; }
    public void setRouteSelectionExpression(String routeSelectionExpression) { this.routeSelectionExpression = routeSelectionExpression; }

    public Map<String, Object> getAuthTypes() { return authTypes; }
    public void setAuthTypes(Map<String, Object> authTypes) { this.authTypes = authTypes; }

    public Map<String, Object> getCorsConfiguration() { return corsConfiguration; }
    public void setCorsConfiguration(Map<String, Object> corsConfiguration) { this.corsConfiguration = corsConfiguration; }

    public Map<String, Object> getStages() { return stages; }
    public void setStages(Map<String, Object> stages) { this.stages = stages; }

    public Map<String, Object> getTags() { return tags; }
    public void setTags(Map<String, Object> tags) { this.tags = tags; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Instant getCollectedAt() { return collectedAt; }
    public void setCollectedAt(Instant collectedAt) { this.collectedAt = collectedAt; }
}
