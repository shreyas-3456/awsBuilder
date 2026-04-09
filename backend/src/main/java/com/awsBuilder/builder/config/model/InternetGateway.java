package com.awsBuilder.builder.config.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;

/**
 * JPA entity for AWS internet gateways.
 * Maps to aws_internet_gateways table.
 */
@Entity
@Table(name = "aws_internet_gateways")
public class InternetGateway {
    @Id
    @Column(name = "igw_id")
    private String igwId;

    @Column(name = "region")
    private String region;

    @Column(name = "state")
    private String state;

    @Column(name = "vpc_id")
    private String vpcId;

    @Column(name = "tags", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> tags;

    @Column(name = "collected_at")
    private Instant collectedAt;

    // Getters and setters
    public String getIgwId() {
        return igwId;
    }

    public void setIgwId(String igwId) {
        this.igwId = igwId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
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
