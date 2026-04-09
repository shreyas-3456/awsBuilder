package com.awsBuilder.builder.config.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "aws_cloudwatch_alarms")
public class CloudWatchAlarm {
    @Id
    @Column(name = "alarm_arn")
    private String alarmArn;

    @Column(name = "alarm_name")
    private String alarmName;

    @Column(name = "region")
    private String region;

    @Column(name = "metric_name")
    private String metricName;

    @Column(name = "namespace")
    private String namespace;

    @Column(name = "statistic")
    private String statistic;

    @Column(name = "period")
    private Integer period;

    @Column(name = "evaluation_periods")
    private Integer evaluationPeriods;

    @Column(name = "threshold")
    private Double threshold;

    @Column(name = "comparison_operator")
    private String comparisonOperator;

    @Column(name = "alarm_description")
    private String alarmDescription;

    @Column(name = "tags", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> tags;

    @Column(name = "source")
    private String source;

    @Column(name = "collected_at")
    private Instant collectedAt;

    public String getAlarmArn() { return alarmArn; }
    public void setAlarmArn(String alarmArn) { this.alarmArn = alarmArn; }
    public String getAlarmName() { return alarmName; }
    public void setAlarmName(String alarmName) { this.alarmName = alarmName; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }
    public String getNamespace() { return namespace; }
    public void setNamespace(String namespace) { this.namespace = namespace; }
    public String getStatistic() { return statistic; }
    public void setStatistic(String statistic) { this.statistic = statistic; }
    public Integer getPeriod() { return period; }
    public void setPeriod(Integer period) { this.period = period; }
    public Integer getEvaluationPeriods() { return evaluationPeriods; }
    public void setEvaluationPeriods(Integer evaluationPeriods) { this.evaluationPeriods = evaluationPeriods; }
    public Double getThreshold() { return threshold; }
    public void setThreshold(Double threshold) { this.threshold = threshold; }
    public String getComparisonOperator() { return comparisonOperator; }
    public void setComparisonOperator(String comparisonOperator) { this.comparisonOperator = comparisonOperator; }
    public String getAlarmDescription() { return alarmDescription; }
    public void setAlarmDescription(String alarmDescription) { this.alarmDescription = alarmDescription; }
    public Map<String, Object> getTags() { return tags; }
    public void setTags(Map<String, Object> tags) { this.tags = tags; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Instant getCollectedAt() { return collectedAt; }
    public void setCollectedAt(Instant collectedAt) { this.collectedAt = collectedAt; }
}
