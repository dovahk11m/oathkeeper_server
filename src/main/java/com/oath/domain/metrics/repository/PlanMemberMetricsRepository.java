// com/oath/domain/metrics/repository/PlanMemberMetricsRepository.java
package com.oath.domain.metrics.repository;

import com.oath.domain.metrics.domain.PlanMemberMetrics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanMemberMetricsRepository extends JpaRepository<PlanMemberMetrics, Long> {
    Optional<PlanMemberMetrics> findByPlanIdAndMemberId(Long planId, Long memberId);
    List<PlanMemberMetrics> findAllByPlanId(Long planId);
}
