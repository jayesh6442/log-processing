package com.jayesh.processorservice.model;

import java.util.Locale;

public enum LogLevel {
    INFO(1),
    WARN(2),
    ERROR(3);

    private final int severity;

    LogLevel(int severity) {
        this.severity = severity;
    }

    public int getSeverity() {
        return severity;
    }

    public static LogLevel from(String value) {
        return LogLevel.valueOf(value.toUpperCase(Locale.ROOT));
    }
}
