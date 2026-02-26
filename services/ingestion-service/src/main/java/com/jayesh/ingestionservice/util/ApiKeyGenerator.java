package com.jayesh.ingestionservice.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ApiKeyGenerator {

    public String generate() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
