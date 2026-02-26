package com.jayesh.processorservice.service;

import com.jayesh.processorservice.dto.AlertKafkaEventDto;
import com.jayesh.processorservice.kafka.AlertEventProducer;
import com.jayesh.processorservice.model.AlertEventEntity;
import com.jayesh.processorservice.model.AlertRuleEntity;
import com.jayesh.processorservice.model.LogEntryEntity;
import com.jayesh.processorservice.repository.AlertEventRepository;
import com.jayesh.processorservice.repository.AlertRuleRepository;
import com.jayesh.processorservice.repository.LogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AlertEvaluationService {

    private static final Logger logger = LoggerFactory.getLogger(AlertEvaluationService.class);

    private final AlertRuleRepository alertRuleRepository;
    private final LogRepository logRepository;
    private final AlertEventRepository alertEventRepository;
    private final AlertEventProducer alertEventProducer;

    public AlertEvaluationService(AlertRuleRepository alertRuleRepository,
                                  LogRepository logRepository,
                                  AlertEventRepository alertEventRepository,
                                  AlertEventProducer alertEventProducer) {
        this.alertRuleRepository = alertRuleRepository;
        this.logRepository = logRepository;
        this.alertEventRepository = alertEventRepository;
        this.alertEventProducer = alertEventProducer;
    }

    @Async
    @Transactional
    public void evaluateAsync(LogEntryEntity logEntry) {
        try {
            List<AlertRuleEntity> rules = alertRuleRepository.findByServiceIdAndLevel(
                    logEntry.getServiceId(), logEntry.getLevel());

            for (AlertRuleEntity rule : rules) {
                if (!rule.getUserId().equals(logEntry.getUserId())) {
                    continue;
                }

                Instant windowStart = logEntry.getTimestamp().minus(rule.getWindowMinutes(), ChronoUnit.MINUTES);
                long count = logRepository.countByUserIdAndServiceIdAndLevelAndTimestampBetween(
                        logEntry.getUserId(),
                        logEntry.getServiceId(),
                        logEntry.getLevel(),
                        windowStart,
                        logEntry.getTimestamp());

                if (count < rule.getThreshold()) {
                    continue;
                }

                boolean alreadyTriggered = alertEventRepository
                        .existsByAlertIdAndTriggeredAtAfter(rule.getId(), windowStart);
                if (alreadyTriggered) {
                    continue;
                }

                AlertEventEntity alertEvent = new AlertEventEntity();
                alertEvent.setAlertId(rule.getId());
                alertEvent.setUserId(logEntry.getUserId());
                alertEvent.setServiceId(logEntry.getServiceId());
                alertEvent.setLevel(logEntry.getLevel());
                alertEvent.setTriggeredAt(logEntry.getTimestamp());
                alertEvent.setLogCount(count);

                AlertEventEntity saved = alertEventRepository.save(alertEvent);

                AlertKafkaEventDto eventDto = new AlertKafkaEventDto();
                eventDto.setAlertId(saved.getAlertId());
                eventDto.setUserId(saved.getUserId());
                eventDto.setServiceId(saved.getServiceId());
                eventDto.setLevel(saved.getLevel());
                eventDto.setLogCount(saved.getLogCount());
                eventDto.setThreshold(rule.getThreshold());
                eventDto.setWindowMinutes(rule.getWindowMinutes());
                eventDto.setTriggeredAt(saved.getTriggeredAt());

                alertEventProducer.publish(eventDto);
                logger.info("event=alert_triggered alertId={} userId={} serviceId={} level={} count={} threshold={} windowMinutes={}",
                        saved.getAlertId(), saved.getUserId(), saved.getServiceId(), saved.getLevel(),
                        saved.getLogCount(), rule.getThreshold(), rule.getWindowMinutes());
            }
        } catch (Exception ex) {
            logger.error("event=alert_evaluation_failed logId={} message={}", logEntry.getId(), ex.getMessage(), ex);
        }
    }
}
