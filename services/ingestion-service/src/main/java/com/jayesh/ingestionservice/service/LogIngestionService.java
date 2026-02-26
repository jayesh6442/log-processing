package com.jayesh.ingestionservice.service;

import com.jayesh.ingestionservice.dto.KafkaLogEvent;
import com.jayesh.ingestionservice.dto.LogIngestionRequest;
import com.jayesh.ingestionservice.dto.LogIngestionResponse;
import com.jayesh.ingestionservice.exception.ForbiddenException;
import com.jayesh.ingestionservice.exception.UnauthorizedException;
import com.jayesh.ingestionservice.kafka.LogEventProducer;
import com.jayesh.ingestionservice.model.ServiceEntity;
import com.jayesh.ingestionservice.security.AuthenticatedUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

@Service
public class LogIngestionService {

    private final ServiceManagementService serviceManagementService;
    private final LogEventProducer logEventProducer;

    public LogIngestionService(ServiceManagementService serviceManagementService,
                               LogEventProducer logEventProducer) {
        this.serviceManagementService = serviceManagementService;
        this.logEventProducer = logEventProducer;
    }

    @Transactional(readOnly = true)
    public LogIngestionResponse ingest(AuthenticatedUser user,
                                       String apiKey,
                                       LogIngestionRequest request) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new UnauthorizedException("Missing API key");
        }

        ServiceEntity service = serviceManagementService.getServiceByApiKey(apiKey);
        if (!service.getUserId().equals(user.getUserId())) {
            throw new ForbiddenException("API key does not belong to authenticated user");
        }

        Instant now = Instant.now();
        KafkaLogEvent event = new KafkaLogEvent();
        event.setUserId(user.getUserId());
        event.setServiceId(service.getId());
        event.setLevel(request.getLevel().toUpperCase(Locale.ROOT));
        event.setMessage(request.getMessage());
        event.setTraceId(request.getTraceId());
        event.setTimestamp(now);

        logEventProducer.publish(event);
        return new LogIngestionResponse("accepted", service.getId(), now);
    }
}
