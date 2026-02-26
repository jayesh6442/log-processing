package com.jayesh.ingestionservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class LogIngestionRequest {

    @NotBlank
    @Pattern(regexp = "INFO|WARN|ERROR", message = "must be one of INFO, WARN, ERROR")
    private String level;

    @NotBlank
    @Size(max = 5000)
    private String message;

    @Size(max = 200)
    private String traceId;

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
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
}
