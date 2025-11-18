package com.oath.domain.plan.event.listener;

import com.oath.domain.metrics.service.MetricsPushService;
import com.oath.domain.metrics.service.MetricsRollupService;
import com.oath.domain.plan.event.PlanCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlanCompletionListener {

    private final MetricsRollupService rollupService;
    private final MetricsPushService pushService;

    @TransactionalEventListener // 트랜잭션 커밋 후에 이벤트 처리
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

        // 4. 통계 데이터 AI 서버로 푸시 (Push)
        // 학원 환경 제약으로 인해 AI 통신 부분은 현재 MetricsOrchestrationController에서 임시 비활성화 상태
        // pushService.pushPlan(planId);
        log.warn("[PlanCompletionListener] MetricsPushService 호출은 학원 환경 제약으로 임시 비활성화됨.");

        // 5. (추후 구현) AI 서버로부터 요약 보고서 수신 및 Plan 엔티티에 저장
        // 이 부분은 MetricsOrchestrationController의 로직을 참고하여 구현될 예정입니다.
        // 현재는 AI 통신이 비활성화되어 있으므로, 이 단계는 건너뜁니다.

        log.info("[PlanCompletionListener] PlanCompletedEvent 처리 완료: planId={}", planId);
    }
}
