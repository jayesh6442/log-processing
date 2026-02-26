package com.jayesh.processorservice.service;

import com.jayesh.processorservice.dto.LogResponseDto;
import com.jayesh.processorservice.model.LogEntryEntity;
import com.jayesh.processorservice.repository.LogRepository;
import com.jayesh.processorservice.specification.LogSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

@Service
public class LogQueryService {

    private final LogRepository logRepository;

    public LogQueryService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Transactional(readOnly = true)
    public Page<LogResponseDto> queryLogs(Long userId,
                                          Long serviceId,
                                          String level,
                                          Instant from,
                                          Instant to,
                                          Pageable pageable) {
        String normalizedLevel = level == null ? null : level.toUpperCase(Locale.ROOT);

        Specification<LogEntryEntity> specification = Specification.where(LogSpecifications.hasUserId(userId))
                .and(LogSpecifications.hasServiceId(serviceId))
                .and(LogSpecifications.hasLevel(normalizedLevel))
                .and(LogSpecifications.timestampFrom(from))
                .and(LogSpecifications.timestampTo(to));

        return logRepository.findAll(specification, pageable)
                .map(this::toResponse);
    }

    private LogResponseDto toResponse(LogEntryEntity entity) {
        return new LogResponseDto(
                entity.getId(),
                entity.getServiceId(),
                entity.getLevel(),
                entity.getSeverity(),
                entity.getMessage(),
                entity.getTraceId(),
                entity.getTimestamp()
        );
    }
}
