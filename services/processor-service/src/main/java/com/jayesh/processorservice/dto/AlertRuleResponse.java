package com.jayesh.processorservice.dto;

import java.time.Instant;

public class AlertRuleResponse {

    private Long id;
    private Long serviceId;
    private String level;
    private Integer threshold;
    private Integer windowMinutes;
    private Instant createdAt;

    public AlertRuleResponse(Long id,
                             Long serviceId,
                             String level,
                             Integer threshold,
                             Integer windowMinutes,
                             Instant createdAt) {
        this.id = id;
        this.serviceId = serviceId;
        this.level = level;
        this.threshold = threshold;
        this.windowMinutes = windowMinutes;
        this.createdAt = createdAt;
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

    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }

    public Integer getWindowMinutes() {
        return windowMinutes;
    }

    public void setWindowMinutes(Integer windowMinutes) {
        this.windowMinutes = windowMinutes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
