package com.jayesh.processorservice.controller;

import com.jayesh.processorservice.dto.LogResponseDto;
import com.jayesh.processorservice.exception.UnauthorizedException;
import com.jayesh.processorservice.security.AuthenticatedUser;
import com.jayesh.processorservice.service.LogQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/logs")
public class LogQueryController {

    private final LogQueryService logQueryService;

    public LogQueryController(LogQueryService logQueryService) {
        this.logQueryService = logQueryService;
    }

    @GetMapping
    public ResponseEntity<Page<LogResponseDto>> getLogs(
            @RequestParam(name = "serviceId", required = false) Long serviceId,
            @RequestParam(name = "level", required = false) String level,
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @PageableDefault(size = 20, sort = "timestamp") Pageable pageable,
            Authentication authentication) {
        AuthenticatedUser user = extractUser(authentication);
        Page<LogResponseDto> response = logQueryService.queryLogs(
                user.getUserId(),
                serviceId,
                level,
                from,
                to,
                pageable
        );
        return ResponseEntity.ok(response);
    }

    private AuthenticatedUser extractUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new UnauthorizedException("Unauthorized");
        }
        return user;
    }
}
