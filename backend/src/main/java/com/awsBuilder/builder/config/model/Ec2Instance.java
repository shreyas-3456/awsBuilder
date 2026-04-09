package com.awsBuilder.builder.config.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;

/**
 * JPA entity for AWS EC2 instances.
 * Maps to aws_ec2_instances table.
 */
@Entity
@Table(name = "aws_ec2_instances")
public class Ec2Instance {
    @Id
    @Column(name = "instance_id")
    private String instanceId;

    @Column(name = "instance_type")
    private String instanceType;

    @Column(name = "name")
    private String name;

    @Column(name = "state")
    private String state;

    @Column(name = "region")
    private String region;

    @Column(name = "vpc_id")
    private String vpcId;

    @Column(name = "subnet_id")
    private String subnetId;

    @Column(name = "availability_zone")
    private String availabilityZone;

    @Column(name = "private_ip")
    private String privateIp;

    @Column(name = "public_ip")
    private String publicIp;

    @Column(name = "ami_id")
    private String amiId;

    @Column(name = "key_name")
    private String keyName;

    @Column(name = "vcpu_count")
    private Integer vcpuCount;

    @Column(name = "memory_gib")
    private Double memoryGib;

    @Column(name = "tags", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> tags;

    @Column(name = "collected_at")
    private Instant collectedAt;

    // Getters and setters
    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    public String getPrivateIp() {
        return privateIp;
    }

    public void setPrivateIp(String privateIp) {
        this.privateIp = privateIp;
    }

    public String getPublicIp() {
        return publicIp;
    }

    public void setPublicIp(String publicIp) {
        this.publicIp = publicIp;
    }

    public String getAmiId() {
        return amiId;
    }

    public void setAmiId(String amiId) {
        this.amiId = amiId;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public Integer getVcpuCount() {
        return vcpuCount;
    }

    public void setVcpuCount(Integer vcpuCount) {
        this.vcpuCount = vcpuCount;
    }

    public Double getMemoryGib() {
        return memoryGib;
    }

    public void setMemoryGib(Double memoryGib) {
        this.memoryGib = memoryGib;
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
