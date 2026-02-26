package com.jayesh.ingestionservice.repository;

import com.jayesh.ingestionservice.model.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {

    Optional<ServiceEntity> findByApiKey(String apiKey);

    Optional<ServiceEntity> findByIdAndUserId(Long id, Long userId);
}
