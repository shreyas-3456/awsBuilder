package com.awsBuilder.builder.config.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;

/**
 * JPA entity for AWS ALB target groups.
 * Maps to aws_alb_target_groups table.
 */
@Entity
@Table(name = "aws_alb_target_groups")
public class AlbTargetGroup {
    @Id
    @Column(name = "tg_arn")
    private String tgArn;

    @Column(name = "tg_name")
    private String tgName;

    @Column(name = "region")
    private String region;

    @Column(name = "protocol")
    private String protocol;

    @Column(name = "port")
    private Integer port;

    @Column(name = "vpc_id")
    private String vpcId;

    @Column(name = "target_type")
    private String targetType;

    @Column(name = "health_check_enabled")
    private Boolean healthCheckEnabled;

    @Column(name = "health_check_protocol")
    private String healthCheckProtocol;

    @Column(name = "health_check_path")
    private String healthCheckPath;

    @Column(name = "tags", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> tags;

    @Column(name = "collected_at")
    private Instant collectedAt;

    // Getters and setters
    public String getTgArn() {
        return tgArn;
    }

    public void setTgArn(String tgArn) {
        this.tgArn = tgArn;
    }

    public String getTgName() {
        return tgName;
    }

    public void setTgName(String tgName) {
        this.tgName = tgName;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public Boolean getHealthCheckEnabled() {
        return healthCheckEnabled;
    }

    public void setHealthCheckEnabled(Boolean healthCheckEnabled) {
        this.healthCheckEnabled = healthCheckEnabled;
    }

    public String getHealthCheckProtocol() {
        return healthCheckProtocol;
    }

    public void setHealthCheckProtocol(String healthCheckProtocol) {
        this.healthCheckProtocol = healthCheckProtocol;
    }

    public String getHealthCheckPath() {
        return healthCheckPath;
    }

    public void setHealthCheckPath(String healthCheckPath) {
        this.healthCheckPath = healthCheckPath;
    }

    public Map<String, Object> getTags() {
        return tags;
    }

    public void setTags(Map<String, Object> tags) {
        this.tags = tags;
    }

    public Instant getCollectedAt() {
        return collectedAt;
    }

    public void setCollectedAt(Instant collectedAt) {
        this.collectedAt = collectedAt;
    }
}
