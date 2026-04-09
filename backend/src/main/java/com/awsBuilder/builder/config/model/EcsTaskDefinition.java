package com.awsBuilder.builder.config.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "aws_ecs_task_definitions")
public class EcsTaskDefinition {
    @Id
    @Column(name = "task_definition_arn")
    private String taskDefinitionArn;

    @Column(name = "family")
    private String family;

    @Column(name = "region")
    private String region;

    @Column(name = "revision")
    private Integer revision;

    @Column(name = "cpu")
    private String cpu;

    @Column(name = "memory")
    private String memory;

    @Column(name = "network_mode")
    private String networkMode;

    @Column(name = "execution_role_arn")
    private String executionRoleArn;

    @Column(name = "task_role_arn")
    private String taskRoleArn;

    @Column(name = "tags", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> tags;

    @Column(name = "source")
    private String source;

    @Column(name = "collected_at")
    private Instant collectedAt;

    public String getTaskDefinitionArn() { return taskDefinitionArn; }
    public void setTaskDefinitionArn(String taskDefinitionArn) { this.taskDefinitionArn = taskDefinitionArn; }
    public String getFamily() { return family; }
    public void setFamily(String family) { this.family = family; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public Integer getRevision() { return revision; }
    public void setRevision(Integer revision) { this.revision = revision; }
    public String getCpu() { return cpu; }
    public void setCpu(String cpu) { this.cpu = cpu; }
    public String getMemory() { return memory; }
    public void setMemory(String memory) { this.memory = memory; }
    public String getNetworkMode() { return networkMode; }
    public void setNetworkMode(String networkMode) { this.networkMode = networkMode; }
    public String getExecutionRoleArn() { return executionRoleArn; }
    public void setExecutionRoleArn(String executionRoleArn) { this.executionRoleArn = executionRoleArn; }
    public String getTaskRoleArn() { return taskRoleArn; }
    public void setTaskRoleArn(String taskRoleArn) { this.taskRoleArn = taskRoleArn; }
    public Map<String, Object> getTags() { return tags; }
    public void setTags(Map<String, Object> tags) { this.tags = tags; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Instant getCollectedAt() { return collectedAt; }
    public void setCollectedAt(Instant collectedAt) { this.collectedAt = collectedAt; }
}
