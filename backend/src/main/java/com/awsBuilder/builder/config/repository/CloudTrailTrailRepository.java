package com.awsBuilder.builder.config.repository;

import com.awsBuilder.builder.config.model.CloudTrailTrail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CloudTrailTrailRepository extends JpaRepository<CloudTrailTrail, String> {
    List<CloudTrailTrail> findByRegion(String region);
}
