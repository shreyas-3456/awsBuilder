package com.awsBuilder.builder.config.repository;

import com.awsBuilder.builder.config.model.EventBridgeRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for EventBridge rules.
 * Provides database access for aws_eventbridge_rules table.
 */
@Repository
public interface EventBridgeRuleRepository extends JpaRepository<EventBridgeRule, String> {
    List<EventBridgeRule> findByRegion(String region);
}
