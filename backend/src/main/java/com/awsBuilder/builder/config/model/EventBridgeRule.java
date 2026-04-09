package com.awsBuilder.builder.config.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;

/**
 * JPA entity for AWS EventBridge rules.
 * Maps to aws_eventbridge_rules table.
 */
@Entity
@Table(name = "aws_eventbridge_rules")
public class EventBridgeRule {
    @Id
    @Column(name = "rule_arn")
    private String ruleArn;

    @Column(name = "rule_name")
    private String ruleName;

    @Column(name = "region")
    private String region;

    @Column(name = "event_bus_name")
    private String eventBusName;

    @Column(name = "description")
    private String description;

    @Column(name = "state")
    private String state;

    @Column(name = "schedule_expression")
    private String scheduleExpression;

    @Column(name = "event_pattern", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> eventPattern;

    @Column(name = "role_arn")
    private String roleArn;

    @Column(name = "managed_by")
    private String managedBy;

    @Column(name = "targets", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> targets;

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
    public String getRuleArn() { return ruleArn; }
    public void setRuleArn(String ruleArn) { this.ruleArn = ruleArn; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getEventBusName() { return eventBusName; }
    public void setEventBusName(String eventBusName) { this.eventBusName = eventBusName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getScheduleExpression() { return scheduleExpression; }
    public void setScheduleExpression(String scheduleExpression) { this.scheduleExpression = scheduleExpression; }

    public Map<String, Object> getEventPattern() { return eventPattern; }
    public void setEventPattern(Map<String, Object> eventPattern) { this.eventPattern = eventPattern; }

    public String getRoleArn() { return roleArn; }
    public void setRoleArn(String roleArn) { this.roleArn = roleArn; }

    public String getManagedBy() { return managedBy; }
    public void setManagedBy(String managedBy) { this.managedBy = managedBy; }

    public Map<String, Object> getTargets() { return targets; }
    public void setTargets(Map<String, Object> targets) { this.targets = targets; }

    public Map<String, Object> getTags() { return tags; }
    public void setTags(Map<String, Object> tags) { this.tags = tags; }

    public Map<String, Object> getRawConfig() { return rawConfig; }
    public void setRawConfig(Map<String, Object> rawConfig) { this.rawConfig = rawConfig; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Instant getCollectedAt() { return collectedAt; }
    public void setCollectedAt(Instant collectedAt) { this.collectedAt = collectedAt; }
}
