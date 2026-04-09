package com.awsBuilder.builder.config.repository;

import com.awsBuilder.builder.config.model.ApiGateway;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for API Gateways.
 * Provides database access for aws_api_gateways table.
 */
@Repository
public interface ApiGatewayRepository extends JpaRepository<ApiGateway, String> {
    List<ApiGateway> findByRegion(String region);
}
