package com.awsBuilder.builder.config.repository;

import com.awsBuilder.builder.config.model.SqsQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for SQS queues.
 * Provides database access for aws_sqs_queues table.
 */
@Repository
public interface SqsQueueRepository extends JpaRepository<SqsQueue, String> {
    List<SqsQueue> findByRegion(String region);
}
