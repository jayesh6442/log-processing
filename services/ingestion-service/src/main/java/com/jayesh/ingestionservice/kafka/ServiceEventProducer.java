package com.jayesh.ingestionservice.kafka;

import com.jayesh.ingestionservice.dto.KafkaServiceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ServiceEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(ServiceEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public ServiceEventProducer(KafkaTemplate<String, Object> kafkaTemplate,
                                @Value("${app.kafka.topics.service-events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publishCreated(KafkaServiceEvent event) {
        kafkaTemplate.send(topic, String.valueOf(event.getServiceId()), event);
        logger.info("event=service_published topic={} serviceId={} userId={} name={}",
                topic, event.getServiceId(), event.getUserId(), event.getName());
    }
}
