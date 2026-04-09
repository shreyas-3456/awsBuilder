package com.awsBuilder.builder.config.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * JPA entity for AWS S3 buckets.
 * Maps to aws_s3_buckets table.
 */
@Entity
@Table(name = "aws_s3_buckets")
public class S3Bucket {
    @Id
    @Column(name = "bucket_name")
    private String bucketName;

    @Column(name = "region")
    private String region;

    @Column(name = "creation_date")
    private Instant creationDate;

    @Column(name = "versioning")
    private String versioning;

    @Column(name = "encryption_rules", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private List<Map<String, Object>> encryptionRules;

    @Column(name = "public_access_block", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> publicAccessBlock;

    @Column(name = "tags", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> tags;

    @Column(name = "collected_at")
    private Instant collectedAt;

    // Getters and setters
    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public String getVersioning() {
        return versioning;
    }

    public void setVersioning(String versioning) {
        this.versioning = versioning;
    }

    public List<Map<String, Object>> getEncryptionRules() {
        return encryptionRules;
    }

    public void setEncryptionRules(List<Map<String, Object>> encryptionRules) {
        this.encryptionRules = encryptionRules;
    }

    public Map<String, Object> getPublicAccessBlock() {
        return publicAccessBlock;
    }

    public void setPublicAccessBlock(Map<String, Object> publicAccessBlock) {
        this.publicAccessBlock = publicAccessBlock;
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
