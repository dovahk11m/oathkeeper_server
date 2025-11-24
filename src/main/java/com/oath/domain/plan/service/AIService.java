// AI 서버와 통신하여 약속 요약 정보를 비동기로 생성하고 Plan 엔티티를 업데이트합니다.
package com.oath.domain.plan.service;

import com.oath.domain.metrics.service.MetricsPushService;
import com.oath.domain.plan.SummaryStatus;
import com.oath.domain.plan.domain.Plan;
import com.oath.domain.plan.repository.PlanJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final PlanJpaRepository planJpaRepository;
    private final MetricsPushService metricsPushService;

    @Async
    @Transactional("h2TransactionManager")
    public CompletableFuture<Void> generateAndSaveSummary(Long planId) {
        try {
            // MetricsPushService의 메소드를 호출하여 AI 서버로부터 요약 보고서를 가져와 저장합니다.
            // 이 메소드는 내부에 재시도 로직을 포함하고 있습니다.
            metricsPushService.fetchAndSaveSummary(planId);

        } catch (Exception e) {
            // fetchAndSaveSummary의 모든 재시도가 실패하면 예외가 발생합니다.
            // 예외를 여기서 잡아서 Plan의 상태를 FAILED로 업데이트합니다.
            log.error("[summary-generation] AI 요약 생성 비동기 작업 최종 실패. planId={}", planId, e);
            Plan plan = planJpaRepository.findById(planId)
                    .orElse(null); // 여기서 또 다른 예외를 던지지 않도록 처리
            if (plan != null) {
                plan.setSummaryStatus(SummaryStatus.FAILED);
                planJpaRepository.save(plan);
            }
        }
        return CompletableFuture.completedFuture(null);
    }
}
