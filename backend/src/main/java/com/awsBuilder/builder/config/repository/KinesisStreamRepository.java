package com.awsBuilder.builder.config.repository;

import com.awsBuilder.builder.config.model.KinesisStream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for Kinesis streams.
 * Provides database access for aws_kinesis_streams table.
 */
@Repository
public interface KinesisStreamRepository extends JpaRepository<KinesisStream, String> {
    List<KinesisStream> findByRegion(String region);
}
