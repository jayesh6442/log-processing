package com.jayesh.ingestionservice.controller;

import com.jayesh.ingestionservice.kafka.LogProducer;
import com.jayesh.ingestionservice.model.LogEvent;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/logs")
public class LogController {

    private final LogProducer producer;

    public LogController(LogProducer producer) {
        this.producer = producer;
    }

    @PostMapping
    public String ingest(@RequestBody LogEvent event) {
        event.setTimestamp(System.currentTimeMillis());
        producer.send(event);
        return "Log accepted";
    }
}