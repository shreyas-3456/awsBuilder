package com.awsBuilder.builder.config.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;

/**
 * JPA entity for AWS subnets.
 * Maps to aws_subnets table.
 */
@Entity
@Table(name = "aws_subnets")
public class Subnet {
    @Id
    @Column(name = "subnet_id")
    private String subnetId;

    @Column(name = "vpc_id")
    private String vpcId;

    @Column(name = "region")
    private String region;

    @Column(name = "availability_zone")
    private String availabilityZone;

    @Column(name = "cidr_block")
    private String cidrBlock;

    @Column(name = "state")
    private String state;

    @Column(name = "map_public_ip_on_launch")
    private Boolean mapPublicIpOnLaunch;

    @Column(name = "available_ip_address_count")
    private Integer availableIpAddressCount;

    @Column(name = "tags", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> tags;

    @Column(name = "collected_at")
    private Instant collectedAt;

    // Getters and setters
    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    public String getCidrBlock() {
        return cidrBlock;
    }

    public void setCidrBlock(String cidrBlock) {
        this.cidrBlock = cidrBlock;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Boolean getMapPublicIpOnLaunch() {
        return mapPublicIpOnLaunch;
    }

    public void setMapPublicIpOnLaunch(Boolean mapPublicIpOnLaunch) {
        this.mapPublicIpOnLaunch = mapPublicIpOnLaunch;
    }

    public Integer getAvailableIpAddressCount() {
        return availableIpAddressCount;
    }

    public void setAvailableIpAddressCount(Integer availableIpAddressCount) {
        this.availableIpAddressCount = availableIpAddressCount;
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
