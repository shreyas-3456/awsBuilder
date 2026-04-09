package com.awsBuilder.builder.config.repository;

import com.awsBuilder.builder.config.model.DynamoDbTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for DynamoDB tables.
 * Provides database access for aws_dynamodb_tables table.
 */
@Repository
public interface DynamoDbTableRepository extends JpaRepository<DynamoDbTable, String> {
    List<DynamoDbTable> findByRegion(String region);
}
