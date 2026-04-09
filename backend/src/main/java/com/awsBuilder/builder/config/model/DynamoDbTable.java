package com.awsBuilder.builder.config.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;

/**
 * JPA entity for AWS DynamoDB tables.
 * Maps to aws_dynamodb_tables table.
 */
@Entity
@Table(name = "aws_dynamodb_tables")
public class DynamoDbTable {
    @Id
    @Column(name = "table_arn")
    private String tableArn;

    @Column(name = "table_name")
    private String tableName;

    @Column(name = "region")
    private String region;

    @Column(name = "table_status")
    private String tableStatus;

    @Column(name = "billing_mode")
    private String billingMode;

    @Column(name = "read_capacity_units")
    private Integer readCapacityUnits;

    @Column(name = "write_capacity_units")
    private Integer writeCapacityUnits;

    @Column(name = "table_class")
    private String tableClass;

    @Column(name = "stream_enabled")
    private Boolean streamEnabled;

    @Column(name = "encryption_type")
    private String encryptionType;

    @Column(name = "point_in_time_recovery")
    private Boolean pointInTimeRecovery;

    @Column(name = "ttl_enabled")
    private Boolean ttlEnabled;

    @Column(name = "description")
    private String description;

    @Column(name = "use_case")
    private String useCase;

    @Column(name = "tags", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> tags;

    @Column(name = "source")
    private String source;

    @Column(name = "collected_at")
    private Instant collectedAt;

    // Getters and setters
    public String getTableArn() { return tableArn; }
    public void setTableArn(String tableArn) { this.tableArn = tableArn; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getTableStatus() { return tableStatus; }
    public void setTableStatus(String tableStatus) { this.tableStatus = tableStatus; }

    public String getBillingMode() { return billingMode; }
    public void setBillingMode(String billingMode) { this.billingMode = billingMode; }

    public Integer getReadCapacityUnits() { return readCapacityUnits; }
    public void setReadCapacityUnits(Integer readCapacityUnits) { this.readCapacityUnits = readCapacityUnits; }

    public Integer getWriteCapacityUnits() { return writeCapacityUnits; }
    public void setWriteCapacityUnits(Integer writeCapacityUnits) { this.writeCapacityUnits = writeCapacityUnits; }

    public String getTableClass() { return tableClass; }
    public void setTableClass(String tableClass) { this.tableClass = tableClass; }

    public Boolean getStreamEnabled() { return streamEnabled; }
    public void setStreamEnabled(Boolean streamEnabled) { this.streamEnabled = streamEnabled; }

    public String getEncryptionType() { return encryptionType; }
    public void setEncryptionType(String encryptionType) { this.encryptionType = encryptionType; }

    public Boolean getPointInTimeRecovery() { return pointInTimeRecovery; }
    public void setPointInTimeRecovery(Boolean pointInTimeRecovery) { this.pointInTimeRecovery = pointInTimeRecovery; }

    public Boolean getTtlEnabled() { return ttlEnabled; }
    public void setTtlEnabled(Boolean ttlEnabled) { this.ttlEnabled = ttlEnabled; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUseCase() { return useCase; }
    public void setUseCase(String useCase) { this.useCase = useCase; }

    public Map<String, Object> getTags() { return tags; }
    public void setTags(Map<String, Object> tags) { this.tags = tags; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Instant getCollectedAt() { return collectedAt; }
    public void setCollectedAt(Instant collectedAt) { this.collectedAt = collectedAt; }
}
