package com.jayesh.processorservice.repository;

import com.jayesh.processorservice.model.ServiceOwnershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceOwnershipRepository extends JpaRepository<ServiceOwnershipEntity, Long> {

    boolean existsByServiceIdAndUserId(Long serviceId, Long userId);
}
