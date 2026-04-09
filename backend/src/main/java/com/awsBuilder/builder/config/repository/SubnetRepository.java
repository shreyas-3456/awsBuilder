package com.awsBuilder.builder.config.repository;

import com.awsBuilder.builder.config.model.Subnet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for Subnets.
 * Provides database access for aws_subnets table.
 */
@Repository
public interface SubnetRepository extends JpaRepository<Subnet, Long> {
    /**
     * Find subnets by VPC ID.
     *
     * @param vpcId the VPC ID
     * @return list of subnets in the specified VPC
     */
    List<Subnet> findByVpcId(String vpcId);

    /**
     * Find subnets by region.
     *
     * @param region the AWS region
     * @return list of subnets in the specified region
     */
    List<Subnet> findByRegion(String region);

    /**
     * Find subnets by VPC ID and region.
     *
     * @param vpcId the VPC ID
     * @param region the AWS region
     * @return list of subnets in the specified VPC and region
     */
    List<Subnet> findByVpcIdAndRegion(String vpcId, String region);
}
