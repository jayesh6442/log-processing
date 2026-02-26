package com.jayesh.processorservice.service;

import com.jayesh.processorservice.dto.ServiceEventDto;
import com.jayesh.processorservice.exception.ForbiddenException;
import com.jayesh.processorservice.model.ServiceOwnershipEntity;
import com.jayesh.processorservice.repository.ServiceOwnershipRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ServiceOwnershipService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceOwnershipService.class);

    private final ServiceOwnershipRepository serviceOwnershipRepository;

    public ServiceOwnershipService(ServiceOwnershipRepository serviceOwnershipRepository) {
        this.serviceOwnershipRepository = serviceOwnershipRepository;
    }

    @Transactional
    public void upsert(ServiceEventDto eventDto) {
        ServiceOwnershipEntity entity = serviceOwnershipRepository.findById(eventDto.getServiceId())
                .orElseGet(ServiceOwnershipEntity::new);

        entity.setServiceId(eventDto.getServiceId());
        entity.setUserId(eventDto.getUserId());
        entity.setName(eventDto.getName());
        entity.setApiKey(eventDto.getApiKey());

        Instant createdAt = eventDto.getCreatedAt() != null ? eventDto.getCreatedAt() : Instant.now();
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(createdAt);
        }
        entity.setUpdatedAt(Instant.now());

        serviceOwnershipRepository.save(entity);
        logger.info("event=service_registry_upserted serviceId={} userId={} name={}",
                entity.getServiceId(), entity.getUserId(), entity.getName());
    }

    @Transactional(readOnly = true)
    public void ensureOwnership(Long userId, Long serviceId) {
        boolean exists = serviceOwnershipRepository.existsByServiceIdAndUserId(serviceId, userId);
        if (!exists) {
            throw new ForbiddenException("Service does not belong to authenticated user");
        }
    }
}
