package com.awsBuilder.builder.config.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * JPA entity for AWS ALB listeners.
 * Maps to aws_alb_listeners table.
 */
@Entity
@Table(name = "aws_alb_listeners")
public class AlbListener {
    @Id
    @Column(name = "listener_arn")
    private String listenerArn;

    @Column(name = "alb_arn")
    private String albArn;

    @Column(name = "region")
    private String region;

    @Column(name = "port")
    private Integer port;

    @Column(name = "protocol")
    private String protocol;

    @Column(name = "ssl_policy")
    private String sslPolicy;

    @Column(name = "certificates", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private List<Map<String, Object>> certificates;

    @Column(name = "default_actions", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private List<Map<String, Object>> defaultActions;

    @Column(name = "tags", columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> tags;

    @Column(name = "collected_at")
    private Instant collectedAt;

    // Getters and setters
    public String getListenerArn() {
        return listenerArn;
    }

    public void setListenerArn(String listenerArn) {
        this.listenerArn = listenerArn;
    }

    public String getAlbArn() {
        return albArn;
    }

    public void setAlbArn(String albArn) {
        this.albArn = albArn;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getSslPolicy() {
        return sslPolicy;
    }

    public void setSslPolicy(String sslPolicy) {
        this.sslPolicy = sslPolicy;
    }

    public List<Map<String, Object>> getCertificates() {
        return certificates;
    }

    public void setCertificates(List<Map<String, Object>> certificates) {
        this.certificates = certificates;
    }

    public List<Map<String, Object>> getDefaultActions() {
        return defaultActions;
    }

    public void setDefaultActions(List<Map<String, Object>> defaultActions) {
        this.defaultActions = defaultActions;
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
