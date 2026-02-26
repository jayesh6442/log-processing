package com.jayesh.processorservice.dto;

import java.time.Instant;

public class LogResponseDto {

    private Long id;
    private Long serviceId;
    private String level;
    private Integer severity;
    private String message;
    private String traceId;
    private Instant timestamp;

    public LogResponseDto(Long id,
                          Long serviceId,
                          String level,
                          Integer severity,
                          String message,
                          String traceId,
                          Instant timestamp) {
        this.id = id;
        this.serviceId = serviceId;
        this.level = level;
        this.severity = severity;
        this.message = message;
        this.traceId = traceId;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Integer getSeverity() {
        return severity;
    }

    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
