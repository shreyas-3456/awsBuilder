package com.awsBuilder.builder.config.repository;

import com.awsBuilder.builder.config.model.EbsVolume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for EBS volumes.
 * Provides database access for aws_ebs_volumes table.
 */
@Repository
public interface EbsVolumeRepository extends JpaRepository<EbsVolume, Long> {
    /**
     * Find EBS volumes by instance ID.
     *
     * @param instanceId the EC2 instance ID
     * @return list of EBS volumes attached to the specified instance
     */
    List<EbsVolume> findByInstanceId(String instanceId);
    List<EbsVolume> findByRegion(String region);
}
