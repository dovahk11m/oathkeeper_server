package com.oath.domain.plan.event.listener;

import com.oath.domain.groups.service.MetricsGroupService;
import com.oath.domain.plan.event.PlanCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlanCompletionListener {

    private final MetricsGroupService metricsGroupService;

    @EventListener
    @Async // 비동기적으로 실행하여 메인 스레드를 블록하지 않음
    public void handlePlanCompletedEvent(PlanCompletedEvent event) {
        log.info("[PlanCompletionListener] PlanCompletedEvent 수신: planId={}, groupId={}", event.getPlanId(), event.getGroupId());
        if (event.getGroupId() != null) {
            log.info("[PlanCompletionListener] 그룹 요약 생성을 요청합니다. groupId={}", event.getGroupId());
            metricsGroupService.requestGroupSummary(event.getGroupId());
        } else {
            log.warn("[PlanCompletionListener] groupId가 null이므로 그룹 요약을 요청하지 않습니다. planId={}", event.getPlanId());
        }
    }
}
