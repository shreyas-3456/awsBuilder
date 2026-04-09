package com.awsBuilder.builder.config.repository;

import com.awsBuilder.builder.config.model.EcsTaskDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EcsTaskDefinitionRepository extends JpaRepository<EcsTaskDefinition, String> {
    List<EcsTaskDefinition> findByRegion(String region);
}
