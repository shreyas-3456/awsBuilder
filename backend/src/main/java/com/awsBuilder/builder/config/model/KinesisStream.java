package com.awsBuilder.builder.config.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;

/**
 * JPA entity for AWS Kinesis streams.
 * Maps to aws_kinesis_streams table.
 */
@Entity
@Table(name = "aws_kinesis_streams")
public class KinesisStream {
    @Id
    @Column(name = "stream_arn")
    private String streamArn;

    @Column(name = "stream_name")
    private String streamName;

    @Column(name = "region")
    private String region;

    @Column(name = "stream_status")
    private String streamStatus;

    @Column(name = "stream_mode")
    private String streamMode;

    @Column(name = "shard_count")
    private Integer shardCount;

    @Column(name = "retention_period_hours")
    private Integer retentionPeriodHours;

    @Column(name = "encryption_type")
    private String encryptionType;

    @Column(name = "kms_key_id")
    private String kmsKeyId;

    @Column(name = "has_enhanced_monitoring")
    private Boolean hasEnhancedMonitoring;

    @Column(name = "tags", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> tags;

    @Column(name = "source")
    private String source;

    @Column(name = "collected_at")
    private Instant collectedAt;

    // Getters and setters
    public String getStreamArn() { return streamArn; }
    public void setStreamArn(String streamArn) { this.streamArn = streamArn; }

    public String getStreamName() { return streamName; }
    public void setStreamName(String streamName) { this.streamName = streamName; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getStreamStatus() { return streamStatus; }
    public void setStreamStatus(String streamStatus) { this.streamStatus = streamStatus; }

    public String getStreamMode() { return streamMode; }
    public void setStreamMode(String streamMode) { this.streamMode = streamMode; }

    public Integer getShardCount() { return shardCount; }
    public void setShardCount(Integer shardCount) { this.shardCount = shardCount; }

    public Integer getRetentionPeriodHours() { return retentionPeriodHours; }
    public void setRetentionPeriodHours(Integer retentionPeriodHours) { this.retentionPeriodHours = retentionPeriodHours; }

    public String getEncryptionType() { return encryptionType; }
    public void setEncryptionType(String encryptionType) { this.encryptionType = encryptionType; }

    public String getKmsKeyId() { return kmsKeyId; }
    public void setKmsKeyId(String kmsKeyId) { this.kmsKeyId = kmsKeyId; }

    public Boolean getHasEnhancedMonitoring() { return hasEnhancedMonitoring; }
    public void setHasEnhancedMonitoring(Boolean hasEnhancedMonitoring) { this.hasEnhancedMonitoring = hasEnhancedMonitoring; }

    public Map<String, Object> getTags() { return tags; }
    public void setTags(Map<String, Object> tags) { this.tags = tags; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Instant getCollectedAt() { return collectedAt; }
    public void setCollectedAt(Instant collectedAt) { this.collectedAt = collectedAt; }
}
