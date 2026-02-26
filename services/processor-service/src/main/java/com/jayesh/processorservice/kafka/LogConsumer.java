package com.jayesh.processorservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayesh.processorservice.dto.KafkaLogEventDto;
import com.jayesh.processorservice.service.LogProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class LogConsumer {

    private static final Logger logger = LoggerFactory.getLogger(LogConsumer.class);

    private final ObjectMapper objectMapper;
    private final LogProcessingService logProcessingService;

    public LogConsumer(ObjectMapper objectMapper, LogProcessingService logProcessingService) {
        this.objectMapper = objectMapper;
        this.logProcessingService = logProcessingService;
    }

    @KafkaListener(topics = "${app.kafka.topics.log-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String message) {
        try {
            KafkaLogEventDto event = objectMapper.readValue(message, KafkaLogEventDto.class);
            validate(event);
            logProcessingService.process(event);
        } catch (Exception ex) {
            logger.error("event=log_consume_failed message={} payload={}", ex.getMessage(), abbreviate(message), ex);
        }
    }

    private void validate(KafkaLogEventDto event) {
        if (event.getUserId() == null || event.getServiceId() == null || event.getLevel() == null
                || event.getMessage() == null || event.getTimestamp() == null) {
            throw new IllegalArgumentException("Invalid log payload");
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
