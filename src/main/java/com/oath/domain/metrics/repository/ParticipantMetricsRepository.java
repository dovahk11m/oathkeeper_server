// com/oath/domain/metrics/repository/ParticipantMetricsRepository.java
package com.oath.domain.metrics.repository;

import com.oath.domain.metrics.domain.ParticipantMetrics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParticipantMetricsRepository extends JpaRepository<ParticipantMetrics, Long> {
    Optional<ParticipantMetrics> findByPlanIdAndMemberId(Long planId, Long memberId);
    List<ParticipantMetrics> findAllByPlanId(Long planId);
}
