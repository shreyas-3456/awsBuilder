package com.awsBuilder.builder.config.repository;

import com.awsBuilder.builder.config.model.AlbTargetGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for ALB Target Groups.
 * Provides database access for aws_alb_target_groups table.
 */
@Repository
public interface AlbTargetGroupRepository extends JpaRepository<AlbTargetGroup, Long> {
    /**
     * Find ALB target groups by VPC ID.
     *
     * @param vpcId the VPC ID
     * @return list of target groups in the specified VPC
     */
    List<AlbTargetGroup> findByVpcId(String vpcId);
}
