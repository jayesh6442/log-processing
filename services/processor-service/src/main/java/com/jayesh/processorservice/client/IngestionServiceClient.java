package com.jayesh.processorservice.client;

import com.jayesh.processorservice.constant.AppConstants;
import com.jayesh.processorservice.exception.ForbiddenException;
import com.jayesh.processorservice.exception.ResourceNotFoundException;
import com.jayesh.processorservice.exception.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

@Component
public class IngestionServiceClient {

    private final RestTemplate ingestionRestTemplate;

    public IngestionServiceClient(RestTemplate ingestionRestTemplate) {
        this.ingestionRestTemplate = ingestionRestTemplate;
    }

    public void verifyServiceOwnership(Long serviceId, String authorizationHeader) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(AppConstants.AUTH_HEADER, authorizationHeader);
            HttpEntity<Void> request = new HttpEntity<>(headers);
            ingestionRestTemplate.exchange(
                    "/api/services/{serviceId}",
                    HttpMethod.GET,
                    request,
                    String.class,
                    serviceId
            );
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                throw new ResourceNotFoundException("Service not found");
            }
            if (ex.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
                throw new ForbiddenException("Service does not belong to authenticated user");
            }
            if (ex.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
                throw new UnauthorizedException("Unauthorized");
            }
            throw new IllegalStateException("Unable to validate service ownership", ex);
        } catch (RestClientException ex) {
            throw new IllegalStateException("Unable to validate service ownership", ex);
        }
    }
}
