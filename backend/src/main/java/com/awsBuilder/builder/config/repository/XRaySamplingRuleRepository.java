package com.awsBuilder.builder.config.repository;

import com.awsBuilder.builder.config.model.XRaySamplingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface XRaySamplingRuleRepository extends JpaRepository<XRaySamplingRule, String> {
    List<XRaySamplingRule> findByRegion(String region);
}
