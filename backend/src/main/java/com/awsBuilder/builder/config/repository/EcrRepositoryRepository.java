package com.awsBuilder.builder.config.repository;

import com.awsBuilder.builder.config.model.EcrRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EcrRepositoryRepository extends JpaRepository<EcrRepository, String> {
    List<EcrRepository> findByRegion(String region);
}
