package com.jayesh.processorservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class LogConsumer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "log-events", groupId = "log-processor-group")
    public void consume(String message) throws Exception {

        System.out.println("Raw JSON: " + message);

        // Later we map to DTO
        // LogEvent event = objectMapper.readValue(message, LogEvent.class);

    }
}