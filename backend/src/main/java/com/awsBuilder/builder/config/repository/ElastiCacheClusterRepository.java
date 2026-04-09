package com.awsBuilder.builder.config.repository;

import com.awsBuilder.builder.config.model.ElastiCacheCluster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for ElastiCache clusters.
 * Provides database access for aws_elasticache_clusters table.
 */
@Repository
public interface ElastiCacheClusterRepository extends JpaRepository<ElastiCacheCluster, String> {
    List<ElastiCacheCluster> findByRegion(String region);
}
