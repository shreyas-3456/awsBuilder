package com.awsBuilder.builder.config.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;

/**
 * JPA entity for AWS Lambda functions.
 * Maps to aws_lambda_functions table.
 */
@Entity
@Table(name = "aws_lambda_functions")
public class LambdaFunction {
    @Id
    @Column(name = "function_arn")
    private String functionArn;

    @Column(name = "function_name")
    private String functionName;

    @Column(name = "region")
    private String region;

    @Column(name = "runtime")
    private String runtime;

    @Column(name = "handler")
    private String handler;

    @Column(name = "memory_mb")
    private Integer memoryMb;

    @Column(name = "memory_min_mb")
    private Integer memoryMinMb;

    @Column(name = "memory_max_mb")
    private Integer memoryMaxMb;

    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds;

    @Column(name = "timeout_max_seconds")
    private Integer timeoutMaxSeconds;

    @Column(name = "ephemeral_storage_max_mb")
    private Integer ephemeralStorageMaxMb;

    @Column(name = "concurrency_limit")
    private Integer concurrencyLimit;

    @Column(name = "language")
    private String language;

    @Column(name = "status")
    private String status;

    @Column(name = "description")
    private String description;

    @Column(name = "supported_architectures", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> supportedArchitectures;

    @Column(name = "tags", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> tags;

    @Column(name = "source")
    private String source;

    @Column(name = "collected_at")
    private Instant collectedAt;

    // Getters and setters
    public String getFunctionArn() { return functionArn; }
    public void setFunctionArn(String functionArn) { this.functionArn = functionArn; }

    public String getFunctionName() { return functionName; }
    public void setFunctionName(String functionName) { this.functionName = functionName; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getRuntime() { return runtime; }
    public void setRuntime(String runtime) { this.runtime = runtime; }

    public String getHandler() { return handler; }
    public void setHandler(String handler) { this.handler = handler; }

    public Integer getMemoryMb() { return memoryMb; }
    public void setMemoryMb(Integer memoryMb) { this.memoryMb = memoryMb; }

    public Integer getMemoryMinMb() { return memoryMinMb; }
    public void setMemoryMinMb(Integer memoryMinMb) { this.memoryMinMb = memoryMinMb; }

    public Integer getMemoryMaxMb() { return memoryMaxMb; }
    public void setMemoryMaxMb(Integer memoryMaxMb) { this.memoryMaxMb = memoryMaxMb; }

    public Integer getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(Integer timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }

    public Integer getTimeoutMaxSeconds() { return timeoutMaxSeconds; }
    public void setTimeoutMaxSeconds(Integer timeoutMaxSeconds) { this.timeoutMaxSeconds = timeoutMaxSeconds; }

    public Integer getEphemeralStorageMaxMb() { return ephemeralStorageMaxMb; }
    public void setEphemeralStorageMaxMb(Integer ephemeralStorageMaxMb) { this.ephemeralStorageMaxMb = ephemeralStorageMaxMb; }

    public Integer getConcurrencyLimit() { return concurrencyLimit; }
    public void setConcurrencyLimit(Integer concurrencyLimit) { this.concurrencyLimit = concurrencyLimit; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Map<String, Object> getSupportedArchitectures() { return supportedArchitectures; }
    public void setSupportedArchitectures(Map<String, Object> supportedArchitectures) { this.supportedArchitectures = supportedArchitectures; }

    public Map<String, Object> getTags() { return tags; }
    public void setTags(Map<String, Object> tags) { this.tags = tags; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Instant getCollectedAt() { return collectedAt; }
    public void setCollectedAt(Instant collectedAt) { this.collectedAt = collectedAt; }
}
