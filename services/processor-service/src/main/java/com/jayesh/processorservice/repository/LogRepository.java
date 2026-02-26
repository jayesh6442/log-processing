package com.jayesh.processorservice.repository;

import com.jayesh.processorservice.model.LogEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;

public interface LogRepository extends JpaRepository<LogEntryEntity, Long>, JpaSpecificationExecutor<LogEntryEntity> {

    long countByUserIdAndServiceIdAndLevelAndTimestampBetween(Long userId,
                                                              Long serviceId,
                                                              String level,
                                                              Instant from,
                                                              Instant to);
}
