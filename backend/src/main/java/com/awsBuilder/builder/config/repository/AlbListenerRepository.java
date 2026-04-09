package com.awsBuilder.builder.config.repository;

import com.awsBuilder.builder.config.model.AlbListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for ALB Listeners.
 * Provides database access for aws_alb_listeners table.
 */
@Repository
public interface AlbListenerRepository extends JpaRepository<AlbListener, Long> {
    /**
     * Find ALB listeners by ALB ARN.
     *
     * @param albArn the ALB ARN
     * @return list of listeners for the specified ALB
     */
    List<AlbListener> findByAlbArn(String albArn);
}
