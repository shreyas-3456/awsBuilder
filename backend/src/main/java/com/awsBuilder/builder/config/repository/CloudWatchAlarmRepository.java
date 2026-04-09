package com.awsBuilder.builder.config.repository;

import com.awsBuilder.builder.config.model.CloudWatchAlarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CloudWatchAlarmRepository extends JpaRepository<CloudWatchAlarm, String> {
    List<CloudWatchAlarm> findByRegion(String region);
}
