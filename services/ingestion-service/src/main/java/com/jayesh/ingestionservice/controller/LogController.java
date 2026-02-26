package com.jayesh.ingestionservice.controller;

import com.jayesh.ingestionservice.constant.AppConstants;
import com.jayesh.ingestionservice.dto.LogIngestionRequest;
import com.jayesh.ingestionservice.dto.LogIngestionResponse;
import com.jayesh.ingestionservice.exception.UnauthorizedException;
import com.jayesh.ingestionservice.security.AuthenticatedUser;
import com.jayesh.ingestionservice.service.LogIngestionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/logs")
public class LogController {

    private final LogIngestionService logIngestionService;

    public LogController(LogIngestionService logIngestionService) {
        this.logIngestionService = logIngestionService;
    }

    @PostMapping
    public ResponseEntity<LogIngestionResponse> ingest(@Valid @RequestBody LogIngestionRequest request,
                                                       @RequestHeader(name = AppConstants.API_KEY_HEADER) String apiKey,
                                                       Authentication authentication) {
        AuthenticatedUser user = extractUser(authentication);
        return ResponseEntity.ok(logIngestionService.ingest(user, apiKey, request));
    }

    private AuthenticatedUser extractUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new UnauthorizedException("Unauthorized");
        }
        return user;
    }
}
