package com.awsBuilder.builder.config.repository;

import com.awsBuilder.builder.config.model.Vpc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for VPCs.
 * Provides database access for aws_vpcs table.
 */
@Repository
public interface VpcRepository extends JpaRepository<Vpc, Long> {
    /**
     * Find VPCs by region.
     *
     * @param region the AWS region
     * @return list of VPCs in the specified region
     */
    List<Vpc> findByRegion(String region);
}
