package com.jayesh.processorservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayesh.processorservice.dto.ServiceEventDto;
import com.jayesh.processorservice.service.ServiceOwnershipService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ServiceEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(ServiceEventConsumer.class);

    private final ObjectMapper objectMapper;
    private final ServiceOwnershipService serviceOwnershipService;

    public ServiceEventConsumer(ObjectMapper objectMapper,
                                ServiceOwnershipService serviceOwnershipService) {
        this.objectMapper = objectMapper;
        this.serviceOwnershipService = serviceOwnershipService;
    }

    @KafkaListener(topics = "${app.kafka.topics.service-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String message) {
        try {
            ServiceEventDto eventDto = objectMapper.readValue(message, ServiceEventDto.class);
            validate(eventDto);
            serviceOwnershipService.upsert(eventDto);
        } catch (Exception ex) {
            logger.error("event=service_consume_failed message={} payload={}", ex.getMessage(), abbreviate(message), ex);
        }
    }

    private void validate(ServiceEventDto eventDto) {
        if (eventDto.getServiceId() == null || eventDto.getUserId() == null || eventDto.getName() == null
                || eventDto.getApiKey() == null) {
            throw new IllegalArgumentException("Invalid service event payload");
        }
    }

    private String abbreviate(String text) {
        int maxLength = 500;
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}
