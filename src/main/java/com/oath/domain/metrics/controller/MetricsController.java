// com/oath/domain/metrics/controller/MetricsController.java
package com.oath.domain.metrics.controller;

import com.oath.domain.metrics.domain.ParticipantMetrics;
import com.oath.domain.metrics.repository.ParticipantMetricsRepository;
import com.oath.domain.metrics.service.MetricsPushService;
import com.oath.domain.metrics.service.MetricsRollupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Profile("local") // 이 컨트롤러는 local 환경에서만 활성화
@Tag(name = "Metrics (내부 테스트용)", description = "통계 계산 및 AI 연동을 수동으로 테스트하기 위한 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsRollupService rollupService;
    private final ParticipantMetricsRepository metricsRepo;
    private final MetricsPushService pushService;

    @Operation(summary = "[1단계] 약속 전체 통계 롤업", description = "특정 약속의 모든 참가자에 대해 위치 기록을 기반으로 멤버별 통계(ParticipantMetrics)를 계산하고 저장합니다.")
    @PostMapping("/plans/{planId}/metrics/rollup")
    public ResponseEntity<Void> rollupPlan(@PathVariable Long planId) {
        rollupService.rebuildForPlan(planId);
        return ResponseEntity.noContent().build(); // 204
    }

    @Operation(summary = "[1.1단계] 특정 참가자 통계 롤업", description = "특정 참가자 한 명에 대해서만 통계를 다시 계산하고 저장합니다.")
    @PostMapping("/plans/{planId}/participants/{participantId}/metrics/rollup")
    public ResponseEntity<Void> rollupParticipant(@PathVariable Long planId,
                                                  @PathVariable Long participantId) {
        rollupService.rebuildForParticipantByParticipantId(planId, participantId);
        return ResponseEntity.noContent().build(); // 204
    }

    @Operation(summary = "[2단계] 약속별 멤버 통계 조회", description = "계산된 멤버별 통계(ParticipantMetrics) 목록을 DB에서 직접 조회합니다.")
    @GetMapping("/plans/{planId}/metrics")
    public ResponseEntity<List<ParticipantMetrics>> getPlanMetrics(@PathVariable Long planId) {
        return ResponseEntity.ok(metricsRepo.findAllByPlanId(planId));
    }

    @Operation(summary = "[3단계] 약속 통계 AI 서버로 전송", description = "계산된 멤버별 통계를 AI 서버로 전송(Push)합니다.")
    @PostMapping("/plans/{planId}/metrics/push")
    public ResponseEntity<Void> pushPlanMetrics(@PathVariable Long planId) {
        pushService.pushPlan(planId);
        return ResponseEntity.accepted().build(); // 202
    }
}
