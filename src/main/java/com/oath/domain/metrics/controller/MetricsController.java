// com/oath/domain/metrics/controller/MetricsController.java
package com.oath.domain.metrics.controller;

import com.oath.domain.metrics.domain.ParticipantMetrics;
import com.oath.domain.metrics.repository.ParticipantMetricsRepository;
import com.oath.domain.metrics.service.MetricsPushService;
import com.oath.domain.metrics.service.MetricsRollupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsRollupService rollupService;
    private final ParticipantMetricsRepository metricsRepo;
    private final MetricsPushService pushService;

    /** 1) 약속 전체 롤업 */
    @PostMapping("/plans/{planId}/metrics/rollup")
    public ResponseEntity<Void> rollupPlan(@PathVariable Long planId) {
        rollupService.rebuildForPlan(planId);
        return ResponseEntity.noContent().build(); // 204
    }

    /** 2) 특정 참가자 롤업 */
    @PostMapping("/plans/{planId}/participants/{participantId}/metrics/rollup")
    public ResponseEntity<Void> rollupParticipant(@PathVariable Long planId,
                                                  @PathVariable Long participantId) {
        rollupService.rebuildForParticipantByParticipantId(planId, participantId);
        return ResponseEntity.noContent().build(); // 204
    }

    /** 3) 약속별 메트릭 조회 */
    @GetMapping("/plans/{planId}/metrics")
    public ResponseEntity<List<ParticipantMetrics>> getPlanMetrics(@PathVariable Long planId) {
        return ResponseEntity.ok(metricsRepo.findAllByPlanId(planId));
    }

    /** 4) 약속 메트릭 AI로 푸시 */
    @PostMapping("/plans/{planId}/metrics/push")
    public ResponseEntity<Void> pushPlanMetrics(@PathVariable Long planId) {
        pushService.pushPlan(planId);
        return ResponseEntity.accepted().build(); // 202
    }
}
