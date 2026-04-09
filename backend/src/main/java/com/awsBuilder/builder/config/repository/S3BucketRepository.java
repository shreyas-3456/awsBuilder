package com.awsBuilder.builder.config.repository;

import com.awsBuilder.builder.config.model.S3Bucket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for S3 buckets.
 * Provides database access for aws_s3_buckets table.
 */
@Repository
public interface S3BucketRepository extends JpaRepository<S3Bucket, Long> {
    /**
     * Find S3 buckets by region.
     *
     * @param region the AWS region
     * @return list of S3 buckets in the specified region
     */
    List<S3Bucket> findByRegion(String region);
}
