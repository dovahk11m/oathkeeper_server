package com.oath.domain.plan.event.listener;

import com.oath.domain.metrics.service.MetricsRollupService;
import com.oath.domain.metrics.service.MockMetricsPushService;
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
    // private final MetricsPushService pushService; // todo
    private final MockMetricsPushService pushService; // Mock 서비스 주입

    /**
     * 플랜 완료 이벤트를 비동기적으로 처리하여, 통계 집계 등 오래 걸릴 수 있는 작업이
     * 원래의 API 응답 시간을 저하시키지 않도록 합니다.
     */
    @Async
    @TransactionalEventListener
    public void handlePlanCompletedEvent(PlanCompletedEvent event) {
        Long planId = event.getPlanId();
        log.info("[PlanCompletionListener] PlanCompletedEvent 수신: planId={}", planId);

        // 1. 멤버별 통계 데이터 생성 (Rollup)
        rollupService.rebuildForPlan(planId);
        log.info("[PlanCompletionListener] MetricsRollupService.rebuildForPlan 완료.");

        // 2. 멤버별 통계를 합산하여 Plan 엔티티에 저장 (Aggregate)
        rollupService.aggregateMetricsForPlan(planId);
        log.info("[PlanCompletionListener] MetricsRollupService.aggregateMetricsForPlan 완료.");

        // 3. 완료된 Plan의 통계를 Group에 누적
        rollupService.accumulatePlanStatsToGroup(planId);
        log.info("[PlanCompletionListener] MetricsRollupService.accumulatePlanStatsToGroup 완료.");

        // 4. 통계 데이터 AI 서버로 푸시 (Push) - Mock Service 호출
        pushService.pushPlan(planId);
        // log.info("[PlanCompletionListener] MetricsPushService.pushPlan 호출 완료."); //todo
        log.info("[PlanCompletionListener] MockMetricsPushService.pushPlan 호출 완료.");

        // 5. (추후 구현) AI 서버로부터 요약 보고서 수신 및 Plan 엔티티에 저장
        // 이 부분은 MetricsOrchestrationController의 로직을 참고하여 구현될 예정입니다.

        log.info("[PlanCompletionListener] PlanCompletedEvent 처리 완료: planId={}", planId);
    }
}
