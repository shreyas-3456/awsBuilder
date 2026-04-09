package com.awsBuilder.builder.config.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * JPA entity for AWS Application Load Balancers.
 * Maps to aws_albs table.
 */
@Entity
@Table(name = "aws_albs")
public class Alb {
    @Id
    @Column(name = "alb_arn")
    private String albArn;

    @Column(name = "alb_name")
    private String albName;

    @Column(name = "region")
    private String region;

    @Column(name = "dns_name")
    private String dnsName;

    @Column(name = "scheme")
    private String scheme;

    @Column(name = "state")
    private String state;

    @Column(name = "vpc_id")
    private String vpcId;

    @Column(name = "type")
    private String type;

    @Column(name = "security_groups", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private List<String> securityGroups;

    @Column(name = "availability_zones", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private List<Map<String, Object>> availabilityZones;

    @Column(name = "tags", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> tags;

    @Column(name = "collected_at")
    private Instant collectedAt;

    // Getters and setters
    public String getAlbArn() {
        return albArn;
    }

    public void setAlbArn(String albArn) {
        this.albArn = albArn;
    }

    public String getAlbName() {
        return albName;
    }

    public void setAlbName(String albName) {
        this.albName = albName;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getDnsName() {
        return dnsName;
    }

    public void setDnsName(String dnsName) {
        this.dnsName = dnsName;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getSecurityGroups() {
        return securityGroups;
    }

    public void setSecurityGroups(List<String> securityGroups) {
        this.securityGroups = securityGroups;
    }

    public List<Map<String, Object>> getAvailabilityZones() {
        return availabilityZones;
    }

    public void setAvailabilityZones(List<Map<String, Object>> availabilityZones) {
        this.availabilityZones = availabilityZones;
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
