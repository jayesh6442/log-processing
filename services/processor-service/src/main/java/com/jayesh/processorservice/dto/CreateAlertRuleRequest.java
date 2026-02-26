package com.jayesh.processorservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class CreateAlertRuleRequest {

    @NotNull
    private Long serviceId;

    @Pattern(regexp = "INFO|WARN|ERROR", message = "must be one of INFO, WARN, ERROR")
    private String level;

    @NotNull
    @Min(1)
    private Integer threshold;

    @NotNull
    @Min(1)
    private Integer windowMinutes;

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
}
