package com.awsBuilder.builder.config.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;

/**
 * JPA entity for AWS EBS volumes.
 * Maps to aws_ebs_volumes table.
 */
@Entity
@Table(name = "aws_ebs_volumes")
public class EbsVolume {
    @Id
    @Column(name = "volume_id")
    private String volumeId;

    @Column(name = "volume_type")
    private String volumeType;

    @Column(name = "size_gb")
    private Integer sizeGb;

    @Column(name = "state")
    private String state;

    @Column(name = "encrypted")
    private Boolean encrypted;

    @Column(name = "region")
    private String region;

    @Column(name = "availability_zone")
    private String availabilityZone;

    @Column(name = "instance_id")
    private String instanceId;

    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "iops")
    private Integer iops;

    @Column(name = "throughput")
    private Integer throughput;

    @Column(name = "tags", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> tags;

    @Column(name = "collected_at")
    private Instant collectedAt;

    // Getters and setters
    public String getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(String volumeId) {
        this.volumeId = volumeId;
    }

    public String getVolumeType() {
        return volumeType;
    }

    public void setVolumeType(String volumeType) {
        this.volumeType = volumeType;
    }

    public Integer getSizeGb() {
        return sizeGb;
    }

    public void setSizeGb(Integer sizeGb) {
        this.sizeGb = sizeGb;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Boolean getEncrypted() {
        return encrypted;
    }

    public void setEncrypted(Boolean encrypted) {
        this.encrypted = encrypted;
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

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Integer getIops() {
        return iops;
    }

    public void setIops(Integer iops) {
        this.iops = iops;
    }

    public Integer getThroughput() {
        return throughput;
    }

    public void setThroughput(Integer throughput) {
        this.throughput = throughput;
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
