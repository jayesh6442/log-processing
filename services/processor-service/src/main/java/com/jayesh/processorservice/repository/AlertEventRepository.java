package com.jayesh.processorservice.repository;

import com.jayesh.processorservice.model.AlertEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface AlertEventRepository extends JpaRepository<AlertEventEntity, Long> {

    boolean existsByAlertIdAndTriggeredAtAfter(Long alertId, Instant triggeredAt);
}
