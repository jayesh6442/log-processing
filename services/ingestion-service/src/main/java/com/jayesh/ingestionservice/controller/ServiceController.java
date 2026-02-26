package com.jayesh.ingestionservice.controller;

import com.jayesh.ingestionservice.dto.CreateServiceRequest;
import com.jayesh.ingestionservice.dto.ServiceResponse;
import com.jayesh.ingestionservice.exception.UnauthorizedException;
import com.jayesh.ingestionservice.security.AuthenticatedUser;
import com.jayesh.ingestionservice.service.ServiceManagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final ServiceManagementService serviceManagementService;

    public ServiceController(ServiceManagementService serviceManagementService) {
        this.serviceManagementService = serviceManagementService;
    }

    @PostMapping
    public ResponseEntity<ServiceResponse> createService(@Valid @RequestBody CreateServiceRequest request,
                                                         Authentication authentication) {
        AuthenticatedUser user = extractUser(authentication);
        ServiceResponse response = serviceManagementService.createService(user.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{serviceId}")
    public ResponseEntity<ServiceResponse> getService(@PathVariable("serviceId") Long serviceId,
                                                      Authentication authentication) {
        AuthenticatedUser user = extractUser(authentication);
        return ResponseEntity.ok(serviceManagementService.getService(user.getUserId(), serviceId));
    }

    private AuthenticatedUser extractUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new UnauthorizedException("Unauthorized");
        }
        return user;
    }
}
