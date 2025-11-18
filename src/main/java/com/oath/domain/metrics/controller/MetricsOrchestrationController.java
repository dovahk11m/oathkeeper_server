// com/oath/domain/metrics/controller/MetricsOrchestrationController.java
package com.oath.domain.metrics.controller;

import com.oath.domain.metrics.service.MetricsRollupService;
import com.oath.domain.metrics.service.MetricsPushService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

@Profile("local") // 이 컨트롤러는 local 환경에서만 활성화
@Tag(name = "Metrics (내부 테스트용)", description = "통계 계산 및 AI 연동을 수동으로 테스트하기 위한 API")
@Slf4j
@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class MetricsOrchestrationController {

    private final MetricsRollupService rollupService;
    private final MetricsPushService pushService;
    private final WebClient aiClient; // http://localhost:8001

    @Operation(summary = "[전체 파이프라인 실행] 통계 계산 > AI 전송 > 결과 수신", description = "통계 계산부터 AI 서버 연동까지의 모든 과정을 순차적으로 실행합니다.")
    @PostMapping("/{planId}/metrics/refresh")
    public ResponseEntity<String> refreshMetrics(@PathVariable Long planId) {
        log.info("[MetricsOrchestrationController] refreshMetrics 호출: planId={}", planId);

        // 1단계: 데이터 집계 (Rollup) - 이 부분은 정상 동작해야 합니다.
        rollupService.rebuildForPlan(planId);
        log.info("[MetricsOrchestrationController] Rollup 서비스 완료.");

        // 2단계: 데이터 푸시 (Push) - 학원 환경에서는 Connection Refused 발생하므로 임시 주석 처리
        // pushService.pushPlan(planId);
        log.warn("[MetricsOrchestrationController] MetricsPushService 호출은 학원 환경 제약으로 임시 비활성화됨.");

        // 3단계: AI 서버로부터 요약 보고서 수신 - 학원 환경에서는 Connection Refused 발생하므로 임시 주석 처리
        /*
        String summary = aiClient.get()
                .uri("/metrics/report/{id}", planId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        log.info("[MetricsOrchestrationController] AI 서버로부터 요약 보고서 수신 완료.");
        return ResponseEntity.ok(summary);
        */

        // AI 서버 통신 비활성화 시 임시 응답
        return ResponseEntity.ok("{\"message\": \"AI 서버 통신은 학원 환경 제약으로 비활성화되었습니다. Rollup 서비스는 정상 동작했습니다.\"}");
    }
}
