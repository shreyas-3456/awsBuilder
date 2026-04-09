package com.awsBuilder.builder.config.repository;

import com.awsBuilder.builder.config.model.Ec2Instance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for EC2 instances.
 * Provides database access for aws_ec2_instances table.
 */
@Repository
public interface Ec2InstanceRepository extends JpaRepository<Ec2Instance, Long> {
    /**
     * Find EC2 instances by region.
     *
     * @param region the AWS region
     * @return list of EC2 instances in the specified region
     */
    List<Ec2Instance> findByRegion(String region);
    List<Ec2Instance> findByAmiIdContainingIgnoreCase(String amiId);
    List<Ec2Instance> findByRegionAndAmiIdContainingIgnoreCase(String region, String amiId);
}
