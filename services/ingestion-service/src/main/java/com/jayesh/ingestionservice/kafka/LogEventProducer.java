package com.jayesh.ingestionservice.kafka;

import com.jayesh.ingestionservice.dto.KafkaLogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class LogEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(LogEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public LogEventProducer(KafkaTemplate<String, Object> kafkaTemplate,
                            @Value("${app.kafka.topics.log-events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(KafkaLogEvent event) {
        kafkaTemplate.send(topic, String.valueOf(event.getServiceId()), event);
        logger.info("event=log_published topic={} serviceId={} userId={} level={}",
                topic, event.getServiceId(), event.getUserId(), event.getLevel());
    }
}
