
package com.jayesh.ingestionservice.kafka;

import com.jayesh.ingestionservice.model.LogEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class LogProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public LogProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(LogEvent event) {
        kafkaTemplate.send("log-events", event.getServiceName(), event);
    }
}