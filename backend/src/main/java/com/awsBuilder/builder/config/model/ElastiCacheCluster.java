package com.awsBuilder.builder.config.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;

/**
 * JPA entity for AWS ElastiCache clusters.
 * Maps to aws_elasticache_clusters table.
 */
@Entity
@Table(name = "aws_elasticache_clusters")
public class ElastiCacheCluster {
    @Id
    @Column(name = "cluster_id")
    private String clusterId;

    @Column(name = "cluster_name")
    private String clusterName;

    @Column(name = "region")
    private String region;

    @Column(name = "engine")
    private String engine;
    
    @Column(name = "engine_version")
    private String engineVersion;

    @Column(name = "node_type")
    private String nodeType;

    @Column(name = "num_cache_nodes")
    private Integer numCacheNodes;

    @Column(name = "status")
    private String status;

    @Column(name = "port")
    private Integer port;

    @Column(name = "subnet_group_name")
    private String subnetGroupName;

    @Column(name = "preferred_availability_zone")
    private String preferredAvailabilityZone;

    @Column(name = "preferred_maintenance_window")
    private String preferredMaintenanceWindow;

    @Column(name = "snapshot_retention_limit")
    private Integer snapshotRetentionLimit;

    @Column(name = "snapshot_window")
    private String snapshotWindow;

    @Column(name = "tags", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> tags;

    @Column(name = "raw_config", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> rawConfig;

    @Column(name = "source")
    private String source;

    @Column(name = "collected_at")
    private Instant collectedAt;

    // Getters and setters
    public String getClusterId() { return clusterId; }
    public void setClusterId(String clusterId) { this.clusterId = clusterId; }

    public String getClusterName() { return clusterName; }
    public void setClusterName(String clusterName) { this.clusterName = clusterName; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getEngine() { return engine; }
    public void setEngine(String engine) { this.engine = engine; }

    public String getEngineVersion() { return engineVersion; }
    public void setEngineVersion(String engineVersion) { this.engineVersion = engineVersion; }

    public String getNodeType() { return nodeType; }
    public void setNodeType(String nodeType) { this.nodeType = nodeType; }

    public Integer getNumCacheNodes() { return numCacheNodes; }
    public void setNumCacheNodes(Integer numCacheNodes) { this.numCacheNodes = numCacheNodes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }

    public String getSubnetGroupName() { return subnetGroupName; }
    public void setSubnetGroupName(String subnetGroupName) { this.subnetGroupName = subnetGroupName; }

    public String getPreferredAvailabilityZone() { return preferredAvailabilityZone; }
    public void setPreferredAvailabilityZone(String preferredAvailabilityZone) { this.preferredAvailabilityZone = preferredAvailabilityZone; }

    public String getPreferredMaintenanceWindow() { return preferredMaintenanceWindow; }
    public void setPreferredMaintenanceWindow(String preferredMaintenanceWindow) { this.preferredMaintenanceWindow = preferredMaintenanceWindow; }

    public Integer getSnapshotRetentionLimit() { return snapshotRetentionLimit; }
    public void setSnapshotRetentionLimit(Integer snapshotRetentionLimit) { this.snapshotRetentionLimit = snapshotRetentionLimit; }

    public String getSnapshotWindow() { return snapshotWindow; }
    public void setSnapshotWindow(String snapshotWindow) { this.snapshotWindow = snapshotWindow; }

    public Map<String, Object> getTags() { return tags; }
    public void setTags(Map<String, Object> tags) { this.tags = tags; }

    public Map<String, Object> getRawConfig() { return rawConfig; }
    public void setRawConfig(Map<String, Object> rawConfig) { this.rawConfig = rawConfig; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Instant getCollectedAt() { return collectedAt; }
    public void setCollectedAt(Instant collectedAt) { this.collectedAt = collectedAt; }
}
