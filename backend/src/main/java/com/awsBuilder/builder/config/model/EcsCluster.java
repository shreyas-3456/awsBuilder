package com.awsBuilder.builder.config.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "aws_ecs_clusters")
public class EcsCluster {
    @Id
    @Column(name = "cluster_arn")
    private String clusterArn;

    @Column(name = "cluster_name")
    private String clusterName;

    @Column(name = "region")
    private String region;

    @Column(name = "status")
    private String status;

    @Column(name = "container_insights_enabled")
    private Boolean containerInsightsEnabled;

    @Column(name = "registered_container_instances")
    private Integer registeredContainerInstances;

    @Column(name = "active_services_count")
    private Integer activeServicesCount;

    @Column(name = "running_tasks_count")
    private Integer runningTasksCount;

    @Column(name = "tags", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> tags;

    @Column(name = "source")
    private String source;

    @Column(name = "collected_at")
    private Instant collectedAt;

    public String getClusterArn() { return clusterArn; }
    public void setClusterArn(String clusterArn) { this.clusterArn = clusterArn; }
    public String getClusterName() { return clusterName; }
    public void setClusterName(String clusterName) { this.clusterName = clusterName; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getContainerInsightsEnabled() { return containerInsightsEnabled; }
    public void setContainerInsightsEnabled(Boolean containerInsightsEnabled) { this.containerInsightsEnabled = containerInsightsEnabled; }
    public Integer getRegisteredContainerInstances() { return registeredContainerInstances; }
    public void setRegisteredContainerInstances(Integer registeredContainerInstances) { this.registeredContainerInstances = registeredContainerInstances; }
    public Integer getActiveServicesCount() { return activeServicesCount; }
    public void setActiveServicesCount(Integer activeServicesCount) { this.activeServicesCount = activeServicesCount; }
    public Integer getRunningTasksCount() { return runningTasksCount; }
    public void setRunningTasksCount(Integer runningTasksCount) { this.runningTasksCount = runningTasksCount; }
    public Map<String, Object> getTags() { return tags; }
    public void setTags(Map<String, Object> tags) { this.tags = tags; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Instant getCollectedAt() { return collectedAt; }
    public void setCollectedAt(Instant collectedAt) { this.collectedAt = collectedAt; }
}
