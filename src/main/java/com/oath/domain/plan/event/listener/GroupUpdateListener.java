package com.oath.domain.plan.event.listener;

import com.oath.domain.groups.service.MetricsGroupService;
import com.oath.domain.metrics.service.MetricsRollupService;
import com.oath.domain.plan.event.GroupUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupUpdateListener {

    private final MetricsRollupService rollupService;
    private final MetricsGroupService metricsGroupService;

    @EventListener
    @Async
    public void handleGroupUpdateEvent(GroupUpdateEvent event) {
        Long planId = event.getPlanId();
        Long groupId = event.getGroupId();

        log.info("[GroupUpdateListener] GroupUpdateEvent 수신. groupId: {}에 대한 'Group' 단위 후처리를 시작합니다.", groupId);

        try {
            // 1. 완료된 Plan의 통계를 Group에 누적합니다.
            rollupService.accumulatePlanStatsToGroup(planId);
            // 2. Group 전체에 대한 요약을 AI 서버에 요청합니다.
            metricsGroupService.requestGroupSummary(groupId);

            log.info("[GroupUpdateListener] 'Group' 단위 후처리 완료 (groupId: {})", groupId);

        } catch (Exception e) {
            log.error("[GroupUpdateListener] 'Group' 단위 후처리 중 오류 발생 (groupId: {}): {}", groupId, e.getMessage(), e);
        }
    }
}
