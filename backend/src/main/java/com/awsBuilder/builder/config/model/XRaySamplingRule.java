package com.awsBuilder.builder.config.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "aws_xray_sampling_rules")
public class XRaySamplingRule {
    @Id
    @Column(name = "rule_arn")
    private String ruleArn;

    @Column(name = "rule_name")
    private String ruleName;

    @Column(name = "region")
    private String region;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "fixed_rate")
    private Double fixedRate;

    @Column(name = "reservoir_size")
    private Integer reservoirSize;

    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "service_type")
    private String serviceType;

    @Column(name = "host")
    private String host;

    @Column(name = "http_method")
    private String httpMethod;

    @Column(name = "url_path")
    private String urlPath;

    @Column(name = "resource_arn")
    private String resourceArn;

    @Column(name = "version")
    private Integer version;

    @Column(name = "tags", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> tags;

    @Column(name = "source")
    private String source;

    @Column(name = "collected_at")
    private Instant collectedAt;

    public String getRuleArn() { return ruleArn; }
    public void setRuleArn(String ruleArn) { this.ruleArn = ruleArn; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public Double getFixedRate() { return fixedRate; }
    public void setFixedRate(Double fixedRate) { this.fixedRate = fixedRate; }
    public Integer getReservoirSize() { return reservoirSize; }
    public void setReservoirSize(Integer reservoirSize) { this.reservoirSize = reservoirSize; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }
    public String getUrlPath() { return urlPath; }
    public void setUrlPath(String urlPath) { this.urlPath = urlPath; }
    public String getResourceArn() { return resourceArn; }
    public void setResourceArn(String resourceArn) { this.resourceArn = resourceArn; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public Map<String, Object> getTags() { return tags; }
    public void setTags(Map<String, Object> tags) { this.tags = tags; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Instant getCollectedAt() { return collectedAt; }
    public void setCollectedAt(Instant collectedAt) { this.collectedAt = collectedAt; }
}
