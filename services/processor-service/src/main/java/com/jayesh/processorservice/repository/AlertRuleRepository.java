package com.jayesh.processorservice.repository;

import com.jayesh.processorservice.model.AlertRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlertRuleRepository extends JpaRepository<AlertRuleEntity, Long> {

    List<AlertRuleEntity> findByServiceIdAndLevel(Long serviceId, String level);

    Optional<AlertRuleEntity> findByUserIdAndServiceIdAndLevel(Long userId, Long serviceId, String level);
}
