package com.awsBuilder.builder.config.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;

/**
 * JPA entity for AWS SNS topics.
 * Maps to aws_sns_topics table.
 */
@Entity
@Table(name = "aws_sns_topics")
public class SnsTopic {
    @Id
    @Column(name = "topic_arn")
    private String topicArn;

    @Column(name = "topic_name")
    private String topicName;

    @Column(name = "region")
    private String region;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "fifo_topic")
    private Boolean fifoTopic;

    @Column(name = "content_based_dedup")
    private Boolean contentBasedDedup;

    @Column(name = "kms_master_key_id")
    private String kmsMasterKeyId;

    @Column(name = "subscriptions_confirmed")
    private Integer subscriptionsConfirmed;

    @Column(name = "subscriptions_pending")
    private Integer subscriptionsPending;

    @Column(name = "subscriptions_deleted")
    private Integer subscriptionsDeleted;

    @Column(name = "tags", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> tags;

    @Column(name = "source")
    private String source;

    @Column(name = "collected_at")
    private Instant collectedAt;

    // Getters and setters
    public String getTopicArn() { return topicArn; }
    public void setTopicArn(String topicArn) { this.topicArn = topicArn; }

    public String getTopicName() { return topicName; }
    public void setTopicName(String topicName) { this.topicName = topicName; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Boolean getFifoTopic() { return fifoTopic; }
    public void setFifoTopic(Boolean fifoTopic) { this.fifoTopic = fifoTopic; }

    public Boolean getContentBasedDedup() { return contentBasedDedup; }
    public void setContentBasedDedup(Boolean contentBasedDedup) { this.contentBasedDedup = contentBasedDedup; }

    public String getKmsMasterKeyId() { return kmsMasterKeyId; }
    public void setKmsMasterKeyId(String kmsMasterKeyId) { this.kmsMasterKeyId = kmsMasterKeyId; }

    public Integer getSubscriptionsConfirmed() { return subscriptionsConfirmed; }
    public void setSubscriptionsConfirmed(Integer subscriptionsConfirmed) { this.subscriptionsConfirmed = subscriptionsConfirmed; }

    public Integer getSubscriptionsPending() { return subscriptionsPending; }
    public void setSubscriptionsPending(Integer subscriptionsPending) { this.subscriptionsPending = subscriptionsPending; }

    public Integer getSubscriptionsDeleted() { return subscriptionsDeleted; }
    public void setSubscriptionsDeleted(Integer subscriptionsDeleted) { this.subscriptionsDeleted = subscriptionsDeleted; }

    public Map<String, Object> getTags() { return tags; }
    public void setTags(Map<String, Object> tags) { this.tags = tags; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Instant getCollectedAt() { return collectedAt; }
    public void setCollectedAt(Instant collectedAt) { this.collectedAt = collectedAt; }
}
