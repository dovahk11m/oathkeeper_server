// com/oath/domain/metrics/controller/MetricsController.java
package com.oath.domain.metrics.controller;

import com.oath.domain.metrics.domain.ParticipantMetrics;
import com.oath.domain.metrics.repository.ParticipantMetricsRepository;
import com.oath.domain.metrics.service.MetricsPushService;
import com.oath.domain.metrics.service.MetricsRollupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsRollupService rollupService;
    private final ParticipantMetricsRepository metricsRepo;
    private final MetricsPushService pushService;

    /** 약속 단위 롤업 */
    @PostMapping("/rebuild/{planId}")
    public void rebuildForPlan(@PathVariable Long planId){
        rollupService.rebuildForPlan(planId);
    }

    /** 특정 참가자만 롤업 (필요 시) */
    @PostMapping("/rebuild/{planId}/member/{memberId}/participant/{participantId}")
    public void rebuildForParticipant(@PathVariable Long planId,
                                      @PathVariable Long memberId,
                                      @PathVariable Long participantId){
        rollupService.rebuildForParticipant(planId, memberId, participantId);
    }

    /** 약속별 메트릭 조회 */
    @GetMapping("/plan/{planId}")
    public List<ParticipantMetrics> listByPlan(@PathVariable Long planId){
        return metricsRepo.findAllByPlanId(planId);
    }

    @PostMapping("/push/{planId}")
    public void pushToAi(@PathVariable Long planId){
        pushService.pushPlan(planId);
    }

}
