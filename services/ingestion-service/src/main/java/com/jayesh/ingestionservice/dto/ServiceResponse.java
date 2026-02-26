package com.jayesh.ingestionservice.dto;

import java.time.Instant;

public class ServiceResponse {

    private Long serviceId;
    private String name;
    private String apiKey;
    private Instant createdAt;

    public ServiceResponse(Long serviceId, String name, String apiKey, Instant createdAt) {
        this.serviceId = serviceId;
        this.name = name;
        this.apiKey = apiKey;
        this.createdAt = createdAt;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
