package com.awsBuilder.builder.config.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;

/**
 * JPA entity for AWS SQS queues.
 * Maps to aws_sqs_queues table.
 */
@Entity
@Table(name = "aws_sqs_queues")
public class SqsQueue {
    @Id
    @Column(name = "queue_url")
    private String queueUrl;

    @Column(name = "queue_name")
    private String queueName;

    @Column(name = "region")
    private String region;

    @Column(name = "fifo_queue")
    private Boolean fifoQueue;

    @Column(name = "delay_seconds")
    private Integer delaySeconds;

    @Column(name = "max_message_size")
    private Integer maxMessageSize;

    @Column(name = "message_retention_seconds")
    private Integer messageRetentionSeconds;

    @Column(name = "visibility_timeout")
    private Integer visibilityTimeout;

    @Column(name = "receive_wait_time_seconds")
    private Integer receiveWaitTimeSeconds;

    @Column(name = "content_based_dedup")
    private Boolean contentBasedDedup;

    @Column(name = "kms_master_key_id")
    private String kmsMasterKeyId;

    @Column(name = "tags", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> tags;

    @Column(name = "source")
    private String source;

    @Column(name = "collected_at")
    private Instant collectedAt;

    // Getters and setters
    public String getQueueUrl() { return queueUrl; }
    public void setQueueUrl(String queueUrl) { this.queueUrl = queueUrl; }

    public String getQueueName() { return queueName; }
    public void setQueueName(String queueName) { this.queueName = queueName; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public Boolean getFifoQueue() { return fifoQueue; }
    public void setFifoQueue(Boolean fifoQueue) { this.fifoQueue = fifoQueue; }

    public Integer getDelaySeconds() { return delaySeconds; }
    public void setDelaySeconds(Integer delaySeconds) { this.delaySeconds = delaySeconds; }

    public Integer getMaxMessageSize() { return maxMessageSize; }
    public void setMaxMessageSize(Integer maxMessageSize) { this.maxMessageSize = maxMessageSize; }

    public Integer getMessageRetentionSeconds() { return messageRetentionSeconds; }
    public void setMessageRetentionSeconds(Integer messageRetentionSeconds) { this.messageRetentionSeconds = messageRetentionSeconds; }

    public Integer getVisibilityTimeout() { return visibilityTimeout; }
    public void setVisibilityTimeout(Integer visibilityTimeout) { this.visibilityTimeout = visibilityTimeout; }

    public Integer getReceiveWaitTimeSeconds() { return receiveWaitTimeSeconds; }
    public void setReceiveWaitTimeSeconds(Integer receiveWaitTimeSeconds) { this.receiveWaitTimeSeconds = receiveWaitTimeSeconds; }

    public Boolean getContentBasedDedup() { return contentBasedDedup; }
    public void setContentBasedDedup(Boolean contentBasedDedup) { this.contentBasedDedup = contentBasedDedup; }

    public String getKmsMasterKeyId() { return kmsMasterKeyId; }
    public void setKmsMasterKeyId(String kmsMasterKeyId) { this.kmsMasterKeyId = kmsMasterKeyId; }

    public Map<String, Object> getTags() { return tags; }
    public void setTags(Map<String, Object> tags) { this.tags = tags; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Instant getCollectedAt() { return collectedAt; }
    public void setCollectedAt(Instant collectedAt) { this.collectedAt = collectedAt; }
}
