package com.oath.domain.plan.event.listener;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import com.oath.domain.metrics.service.MetricsPushService;
import com.oath.domain.metrics.service.MetricsRollupService;
import com.oath.domain.plan.event.GroupUpdateEvent;
import com.oath.domain.plan.event.PlanCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlanCompletionListener {

    private final MetricsRollupService rollupService;
    private final MetricsPushService pushService;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    @Async
    public void handlePlanCompletedEvent(PlanCompletedEvent event) {
        Long planId = event.getPlanId();
        log.info(
                "[PlanCompletionListener] PlanCompletedEvent 수신. planId: {}에 대한 'Plan' 단위 후처리를 시작합니다.",
                planId);

        try {
            // 1. 이 Plan에 대한 개인별 통계를 계산하고 Plan 엔티티에 합산합니다.
            rollupService.rebuildForPlan(planId);
            rollupService.aggregateMetricsForPlan(planId);
            log.info("[PlanCompletionListener] Plan 통계 계산 및 집계 완료 (planId: {})", planId);

            // 2. 계산된 통계 데이터를 AI 서버로 전송(Push)합니다.
            pushService.pushPlan(planId);
            log.info("[PlanCompletionListener] Plan 통계 데이터 AI 서버로 전송 완료 (planId: {})", planId);

            // 3. AI 서버로부터 'Plan' 요약을 가져와서(Fetch) 저장합니다.
            // fetchAndSavePlanSummary는 @Retryable로 구성되어 있어, AI 서버가 준비되지 않았을 경우 자동으로 재시도합니다.
            pushService.fetchAndSavePlanSummary(planId);
            log.info("[PlanCompletionListener] 'Plan' 요약 수신 및 저장 완료 (planId: {})", planId);

            // 5. Plan 처리가 성공적으로 끝나면, 다음 단계를 위한 GroupUpdateEvent를 발행합니다.
            if (event.getGroupId() != null) {
                eventPublisher.publishEvent(new GroupUpdateEvent(planId, event.getGroupId()));
                log.info("[PlanCompletionListener] GroupUpdateEvent 발행 (planId: {}, groupId: {})",
                        planId, event.getGroupId());
            }

        } catch (Exception e) {
            log.error("[PlanCompletionListener] 'Plan' 단위 후처리 중 오류 발생 (planId: {}): {}", planId,
                    e.getMessage(), e);
        }
    }
}
