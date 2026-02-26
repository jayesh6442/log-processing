package com.jayesh.ingestionservice.dto;

import java.time.Instant;

public class LogIngestionResponse {

    private String status;
    private Long serviceId;
    private Instant timestamp;

    public LogIngestionResponse(String status, Long serviceId, Instant timestamp) {
        this.status = status;
        this.serviceId = serviceId;
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
