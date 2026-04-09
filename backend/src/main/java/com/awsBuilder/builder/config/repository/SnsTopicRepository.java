package com.awsBuilder.builder.config.repository;

import com.awsBuilder.builder.config.model.SnsTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for SNS topics.
 * Provides database access for aws_sns_topics table.
 */
@Repository
public interface SnsTopicRepository extends JpaRepository<SnsTopic, String> {
    List<SnsTopic> findByRegion(String region);
}
