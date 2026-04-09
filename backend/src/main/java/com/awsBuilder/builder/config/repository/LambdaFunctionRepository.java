package com.awsBuilder.builder.config.repository;

import com.awsBuilder.builder.config.model.LambdaFunction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for Lambda functions.
 * Provides database access for aws_lambda_functions table.
 */
@Repository
public interface LambdaFunctionRepository extends JpaRepository<LambdaFunction, String> {
    List<LambdaFunction> findByRegion(String region);
}
