package com.awsBuilder.builder.config.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "aws_ecr_repositories")
public class EcrRepository {
    @Id
    @Column(name = "repository_arn")
    private String repositoryArn;

    @Column(name = "repository_name")
    private String repositoryName;

    @Column(name = "region")
    private String region;

    @Column(name = "registry_id")
    private String registryId;

    @Column(name = "repository_uri")
    private String repositoryUri;

    @Column(name = "image_tag_mutability")
    private String imageTagMutability;

    @Column(name = "scan_on_push")
    private Boolean scanOnPush;

    @Column(name = "encryption_type")
    private String encryptionType;

    @Column(name = "kms_key_id")
    private String kmsKeyId;

    @Column(name = "tags", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> tags;

    @Column(name = "source")
    private String source;

    @Column(name = "collected_at")
    private Instant collectedAt;

    public String getRepositoryArn() { return repositoryArn; }
    public void setRepositoryArn(String repositoryArn) { this.repositoryArn = repositoryArn; }
    public String getRepositoryName() { return repositoryName; }
    public void setRepositoryName(String repositoryName) { this.repositoryName = repositoryName; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getRegistryId() { return registryId; }
    public void setRegistryId(String registryId) { this.registryId = registryId; }
    public String getRepositoryUri() { return repositoryUri; }
    public void setRepositoryUri(String repositoryUri) { this.repositoryUri = repositoryUri; }
    public String getImageTagMutability() { return imageTagMutability; }
    public void setImageTagMutability(String imageTagMutability) { this.imageTagMutability = imageTagMutability; }
    public Boolean getScanOnPush() { return scanOnPush; }
    public void setScanOnPush(Boolean scanOnPush) { this.scanOnPush = scanOnPush; }
    public String getEncryptionType() { return encryptionType; }
    public void setEncryptionType(String encryptionType) { this.encryptionType = encryptionType; }
    public String getKmsKeyId() { return kmsKeyId; }
    public void setKmsKeyId(String kmsKeyId) { this.kmsKeyId = kmsKeyId; }
    public Map<String, Object> getTags() { return tags; }
    public void setTags(Map<String, Object> tags) { this.tags = tags; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Instant getCollectedAt() { return collectedAt; }
    public void setCollectedAt(Instant collectedAt) { this.collectedAt = collectedAt; }
}
