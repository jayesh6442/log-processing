package com.jayesh.processorservice.service;

import com.jayesh.processorservice.dto.KafkaLogEventDto;
import com.jayesh.processorservice.model.LogEntryEntity;
import com.jayesh.processorservice.model.LogLevel;
import com.jayesh.processorservice.repository.LogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class LogProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(LogProcessingService.class);

    private final LogRepository logRepository;
    private final AlertEvaluationService alertEvaluationService;

    public LogProcessingService(LogRepository logRepository,
                                AlertEvaluationService alertEvaluationService) {
        this.logRepository = logRepository;
        this.alertEvaluationService = alertEvaluationService;
    }

    @Transactional
    public void process(KafkaLogEventDto event) {
        LogLevel logLevel = LogLevel.from(event.getLevel());
        Instant timestamp = event.getTimestamp() != null ? event.getTimestamp() : Instant.now();

        LogEntryEntity entry = new LogEntryEntity();
        entry.setUserId(event.getUserId());
        entry.setServiceId(event.getServiceId());
        entry.setLevel(logLevel.name());
        entry.setSeverity(logLevel.getSeverity());
        entry.setMessage(event.getMessage());
        entry.setTraceId(event.getTraceId());
        entry.setTimestamp(timestamp);
        entry.setTimeBucket(timestamp.truncatedTo(ChronoUnit.HOURS).toString());

        LogEntryEntity saved = logRepository.save(entry);
        logger.info("event=log_persisted logId={} userId={} serviceId={} level={}",
                saved.getId(), saved.getUserId(), saved.getServiceId(), saved.getLevel());

        alertEvaluationService.evaluateAsync(saved);
    }
}
