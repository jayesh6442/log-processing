package com.jayesh.ingestionservice.service;

import com.jayesh.ingestionservice.dto.CreateServiceRequest;
import com.jayesh.ingestionservice.dto.KafkaServiceEvent;
import com.jayesh.ingestionservice.dto.ServiceResponse;
import com.jayesh.ingestionservice.exception.ResourceNotFoundException;
import com.jayesh.ingestionservice.kafka.ServiceEventProducer;
import com.jayesh.ingestionservice.model.ServiceEntity;
import com.jayesh.ingestionservice.repository.ServiceRepository;
import com.jayesh.ingestionservice.util.ApiKeyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServiceManagementService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceManagementService.class);

    private final ServiceRepository serviceRepository;
    private final ApiKeyGenerator apiKeyGenerator;
    private final ServiceEventProducer serviceEventProducer;

    public ServiceManagementService(ServiceRepository serviceRepository,
                                    ApiKeyGenerator apiKeyGenerator,
                                    ServiceEventProducer serviceEventProducer) {
        this.serviceRepository = serviceRepository;
        this.apiKeyGenerator = apiKeyGenerator;
        this.serviceEventProducer = serviceEventProducer;
    }

    @Transactional
    public ServiceResponse createService(Long userId, CreateServiceRequest request) {
        ServiceEntity entity = new ServiceEntity();
        entity.setName(request.getName().trim());
        entity.setUserId(userId);
        entity.setApiKey(apiKeyGenerator.generate());

        ServiceEntity saved = serviceRepository.save(entity);
        logger.info("event=service_created serviceId={} userId={} name={}", saved.getId(), userId, saved.getName());
        publishServiceCreated(saved);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ServiceResponse getService(Long userId, Long serviceId) {
        ServiceEntity entity = serviceRepository.findByIdAndUserId(serviceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public ServiceEntity getServiceByApiKey(String apiKey) {
        return serviceRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
    }

    private ServiceResponse toResponse(ServiceEntity entity) {
        return new ServiceResponse(
                entity.getId(),
                entity.getName(),
                entity.getApiKey(),
                entity.getCreatedAt()
        );
    }

    private void publishServiceCreated(ServiceEntity entity) {
        KafkaServiceEvent event = new KafkaServiceEvent();
        event.setServiceId(entity.getId());
        event.setUserId(entity.getUserId());
        event.setName(entity.getName());
        event.setApiKey(entity.getApiKey());
        event.setCreatedAt(entity.getCreatedAt());
        serviceEventProducer.publishCreated(event);
    }
}
