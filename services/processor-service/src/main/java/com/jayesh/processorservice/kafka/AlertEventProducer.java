package com.jayesh.processorservice.kafka;

import com.jayesh.processorservice.dto.AlertKafkaEventDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class AlertEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(AlertEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String alertsTopic;

    public AlertEventProducer(KafkaTemplate<String, Object> kafkaTemplate,
                              @Value("${app.kafka.topics.alerts}") String alertsTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.alertsTopic = alertsTopic;
    }

    public void publish(AlertKafkaEventDto eventDto) {
        try {
            kafkaTemplate
                    .send(alertsTopic, String.valueOf(eventDto.getServiceId()), eventDto)
                    .get(5, TimeUnit.SECONDS);
            logger.info("event=alert_published topic={} alertId={} serviceId={} userId={}",
                    alertsTopic, eventDto.getAlertId(), eventDto.getServiceId(), eventDto.getUserId());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while publishing alert event", ex);
        } catch (ExecutionException | TimeoutException ex) {
            throw new IllegalStateException("Failed to publish alert event", ex);
        }
    }
}
