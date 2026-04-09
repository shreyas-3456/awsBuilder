package com.awsBuilder.builder.config.repository;

import com.awsBuilder.builder.config.model.EcsCluster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EcsClusterRepository extends JpaRepository<EcsCluster, String> {
    List<EcsCluster> findByRegion(String region);
}
