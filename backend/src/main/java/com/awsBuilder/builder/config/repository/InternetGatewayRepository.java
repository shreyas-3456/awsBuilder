package com.awsBuilder.builder.config.repository;

import com.awsBuilder.builder.config.model.InternetGateway;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for Internet Gateways.
 * Provides database access for aws_internet_gateways table.
 */
@Repository
public interface InternetGatewayRepository extends JpaRepository<InternetGateway, Long> {
    /**
     * Find internet gateways by VPC ID.
     *
     * @param vpcId the VPC ID
     * @return list of internet gateways attached to the specified VPC
     */
    List<InternetGateway> findByVpcId(String vpcId);
    List<InternetGateway> findByRegion(String region);
}
