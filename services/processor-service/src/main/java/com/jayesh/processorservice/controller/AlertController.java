package com.jayesh.processorservice.controller;

import com.jayesh.processorservice.dto.AlertRuleResponse;
import com.jayesh.processorservice.dto.CreateAlertRuleRequest;
import com.jayesh.processorservice.exception.UnauthorizedException;
import com.jayesh.processorservice.security.AuthenticatedUser;
import com.jayesh.processorservice.service.AlertRuleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

    private final AlertRuleService alertRuleService;

    public AlertController(AlertRuleService alertRuleService) {
        this.alertRuleService = alertRuleService;
    }

    @PostMapping
    public ResponseEntity<AlertRuleResponse> createAlertRule(
            @Valid @RequestBody CreateAlertRuleRequest request,
            Authentication authentication) {
        AuthenticatedUser user = extractUser(authentication);
        AlertRuleResponse response = alertRuleService.createAlertRule(user.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private AuthenticatedUser extractUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new UnauthorizedException("Unauthorized");
        }
        return user;
    }
}
