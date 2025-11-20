package com.oath.domain.plan.event.listener;

import com.oath.domain.metrics.service.MetricsPushService;
import com.oath.domain.metrics.service.MetricsRollupService;
import com.oath.domain.plan.event.PlanCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlanCompletionListener {

    private final MetricsRollupService rollupService;
    private final MetricsPushService pushService;

    /**
     * 플랜 완료 이벤트를 비동기적으로 처리하여, 통계 집계 등 오래 걸릴 수 있는 작업이
     * 원래의 API 응답 시간을 저하시키지 않도록 합니다.
     */
    @Async
    @TransactionalEventListener
    public void handlePlanCompletedEvent(PlanCompletedEvent event) {
        Long planId = event.getPlanId();
        log.info("[PlanCompletionListener] PlanCompletedEvent 수신: planId={}", planId);

        try {
            // 1. 멤버별 통계 데이터 생성 (Rollup)
            rollupService.rebuildForPlan(planId);
            log.info("[PlanCompletionListener] MetricsRollupService.rebuildForPlan 완료.");

            // 2. 멤버별 통계를 합산하여 Plan 엔티티에 저장 (Aggregate)
            rollupService.aggregateMetricsForPlan(planId);
            log.info("[PlanCompletionListener] MetricsRollupService.aggregateMetricsForPlan 완료.");

            // 3. 완료된 Plan의 통계를 Group에 누적
            rollupService.accumulatePlanStatsToGroup(planId);
            log.info("[PlanCompletionListener] MetricsRollupService.accumulatePlanStatsToGroup 완료.");

            // 4. 통계 데이터 AI 서버로 푸시 (Push)
            pushService.pushPlan(planId);
            log.info("[PlanCompletionListener] MetricsPushService.pushPlan 호출 완료.");

            // 5. AI 서버가 데이터를 처리할 시간을 잠시 대기 (5초)
            log.info("[PlanCompletionListener] AI 서버의 분석 시간 대기 시작 (5초)...");
            Thread.sleep(5000);
            log.info("[PlanCompletionListener] AI 서버 분석 시간 대기 완료.");

            // 6. AI 서버로부터 요약 보고서 수신 및 Plan 엔티티에 저장
            pushService.fetchAndSaveSummary(planId);
            log.info("[PlanCompletionListener] MetricsPushService.fetchAndSaveSummary 호출 완료.");

        } catch (InterruptedException e) {
            log.error("[PlanCompletionListener] AI 서버 대기 중 스레드 오류 발생: {}", e.getMessage());
            Thread.currentThread().interrupt(); // 스레드 인터럽트 상태 복원
        } catch (Exception e) {
            log.error("[PlanCompletionListener] 이벤트 처리 중 예외 발생: {}", e.getMessage(), e);
        }

        log.info("[PlanCompletionListener] PlanCompletedEvent 처리 완료: planId={}", planId);
    }
}
