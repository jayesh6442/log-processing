package com.jayesh.processorservice.service;

import com.jayesh.processorservice.dto.AlertRuleResponse;
import com.jayesh.processorservice.dto.CreateAlertRuleRequest;
import com.jayesh.processorservice.exception.ConflictException;
import com.jayesh.processorservice.model.AlertRuleEntity;
import com.jayesh.processorservice.model.LogLevel;
import com.jayesh.processorservice.repository.AlertRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertRuleService {

    private static final Logger logger = LoggerFactory.getLogger(AlertRuleService.class);

    private final AlertRuleRepository alertRuleRepository;
    private final ServiceOwnershipService serviceOwnershipService;

    public AlertRuleService(AlertRuleRepository alertRuleRepository,
                            ServiceOwnershipService serviceOwnershipService) {
        this.alertRuleRepository = alertRuleRepository;
        this.serviceOwnershipService = serviceOwnershipService;
    }

    @Transactional
    public AlertRuleResponse createAlertRule(Long userId, CreateAlertRuleRequest request) {
        serviceOwnershipService.ensureOwnership(userId, request.getServiceId());

        String level = LogLevel.from(request.getLevel()).name();
        alertRuleRepository.findByUserIdAndServiceIdAndLevel(userId, request.getServiceId(), level)
                .ifPresent(rule -> {
                    throw new ConflictException("Alert rule for service and level already exists");
                });

        AlertRuleEntity entity = new AlertRuleEntity();
        entity.setUserId(userId);
        entity.setServiceId(request.getServiceId());
        entity.setLevel(level);
        entity.setThreshold(request.getThreshold());
        entity.setWindowMinutes(request.getWindowMinutes());

        AlertRuleEntity saved = alertRuleRepository.save(entity);
        logger.info("event=alert_rule_created alertRuleId={} userId={} serviceId={} level={} threshold={} windowMinutes={}",
                saved.getId(), saved.getUserId(), saved.getServiceId(), saved.getLevel(),
                saved.getThreshold(), saved.getWindowMinutes());

        return toResponse(saved);
    }

    private AlertRuleResponse toResponse(AlertRuleEntity entity) {
        return new AlertRuleResponse(
                entity.getId(),
                entity.getServiceId(),
                entity.getLevel(),
                entity.getThreshold(),
                entity.getWindowMinutes(),
                entity.getCreatedAt()
        );
    }
}
