package com.awsBuilder.builder.config.repository;

import com.awsBuilder.builder.config.model.Alb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for Application Load Balancers.
 * Provides database access for aws_albs table.
 */
@Repository
public interface AlbRepository extends JpaRepository<Alb, Long> {
    /**
     * Find ALBs by VPC ID.
     *
     * @param vpcId the VPC ID
     * @return list of ALBs in the specified VPC
     */
    List<Alb> findByVpcId(String vpcId);

    /**
     * Find ALBs by region.
     *
     * @param region the AWS region
     * @return list of ALBs in the specified region
     */
    List<Alb> findByRegion(String region);

    /**
     * Find ALBs by VPC ID and region.
     *
     * @param vpcId the VPC ID
     * @param region the AWS region
     * @return list of ALBs in the specified VPC and region
     */
    List<Alb> findByVpcIdAndRegion(String vpcId, String region);
}
