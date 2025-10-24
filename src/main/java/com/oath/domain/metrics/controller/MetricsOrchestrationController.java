// com/oath/domain/metrics/controller/MetricsOrchestrationController.java
package com.oath.domain.metrics.controller;

import com.oath.domain.metrics.service.MetricsRollupService;
import com.oath.domain.metrics.service.MetricsPushService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class MetricsOrchestrationController {

    private final MetricsRollupService rollupService;
    private final MetricsPushService pushService;
    private final WebClient aiClient; // http://localhost:8001

    /** 롤업→푸시→요약 생성 후 요약 JSON 반환 */
    @PostMapping("/{planId}/metrics/refresh")
    public ResponseEntity<String> refreshMetrics(@PathVariable Long planId) {
        rollupService.rebuildForPlan(planId);
        pushService.pushPlan(planId);
        String summary = aiClient.get()
                .uri("/metrics/report/{id}", planId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return ResponseEntity.ok(summary);
    }
}
