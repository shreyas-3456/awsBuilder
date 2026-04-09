package com.awsBuilder.builder.config.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "aws_cloudtrail_trails")
public class CloudTrailTrail {
    @Id
    @Column(name = "trail_arn")
    private String trailArn;

    @Column(name = "trail_name")
    private String trailName;

    @Column(name = "region")
    private String region;

    @Column(name = "s3_bucket_name")
    private String s3BucketName;

    @Column(name = "s3_key_prefix")
    private String s3KeyPrefix;

    @Column(name = "is_multi_region")
    private Boolean isMultiRegion;

    @Column(name = "log_file_validation")
    private Boolean logFileValidation;

    @Column(name = "include_global_events")
    private Boolean includeGlobalEvents;

    @Column(name = "enable_logging")
    private Boolean enableLogging;

    @Column(name = "kms_key_id")
    private String kmsKeyId;

    @Column(name = "sns_topic_arn")
    private String snsTopicArn;

    @Column(name = "cloud_watch_logs_group")
    private String cloudWatchLogsGroup;

    @Column(name = "tags", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> tags;

    @Column(name = "source")
    private String source;

    @Column(name = "collected_at")
    private Instant collectedAt;

    public String getTrailArn() { return trailArn; }
    public void setTrailArn(String trailArn) { this.trailArn = trailArn; }
    public String getTrailName() { return trailName; }
    public void setTrailName(String trailName) { this.trailName = trailName; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getS3BucketName() { return s3BucketName; }
    public void setS3BucketName(String s3BucketName) { this.s3BucketName = s3BucketName; }
    public String getS3KeyPrefix() { return s3KeyPrefix; }
    public void setS3KeyPrefix(String s3KeyPrefix) { this.s3KeyPrefix = s3KeyPrefix; }
    public Boolean getIsMultiRegion() { return isMultiRegion; }
    public void setIsMultiRegion(Boolean isMultiRegion) { this.isMultiRegion = isMultiRegion; }
    public Boolean getLogFileValidation() { return logFileValidation; }
    public void setLogFileValidation(Boolean logFileValidation) { this.logFileValidation = logFileValidation; }
    public Boolean getIncludeGlobalEvents() { return includeGlobalEvents; }
    public void setIncludeGlobalEvents(Boolean includeGlobalEvents) { this.includeGlobalEvents = includeGlobalEvents; }
    public Boolean getEnableLogging() { return enableLogging; }
    public void setEnableLogging(Boolean enableLogging) { this.enableLogging = enableLogging; }
    public String getKmsKeyId() { return kmsKeyId; }
    public void setKmsKeyId(String kmsKeyId) { this.kmsKeyId = kmsKeyId; }
    public String getSnsTopicArn() { return snsTopicArn; }
    public void setSnsTopicArn(String snsTopicArn) { this.snsTopicArn = snsTopicArn; }
    public String getCloudWatchLogsGroup() { return cloudWatchLogsGroup; }
    public void setCloudWatchLogsGroup(String cloudWatchLogsGroup) { this.cloudWatchLogsGroup = cloudWatchLogsGroup; }
    public Map<String, Object> getTags() { return tags; }
    public void setTags(Map<String, Object> tags) { this.tags = tags; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Instant getCollectedAt() { return collectedAt; }
    public void setCollectedAt(Instant collectedAt) { this.collectedAt = collectedAt; }
}
