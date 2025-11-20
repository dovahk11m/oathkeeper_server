package com.oath.domain.metrics.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oath.domain.metrics.repository.ParticipantMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AI 서버와의 실제 통신 없이, 전송될 데이터를 로그로 출력하여 API 연동을 시뮬레이션하는 Mock Service 입니다.
 * local 환경에서 테스트 용도로 사용됩니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MockMetricsPushService {

    private final ParticipantMetricsRepository metricsRepo;
    private final ObjectMapper objectMapper;

    public int pushPlan(Long planId) {
        var rows = metricsRepo.findAllByPlanId(planId);
        log.info("[MOCK-PUSH] planId={} 에 대한 AI 서버 전송 시뮬레이션을 시작합니다. (대상: {}건)", planId, rows.size());

        int ok = 0;

        for (var m : rows) {
            try {
                double distKm = (m.getDistanceKm() != null) ? m.getDistanceKm() : 0.0;
                int tMin = (m.getTravelMinutes() != null) ? m.getTravelMinutes() : 0;

                Map<String, Object> body = new LinkedHashMap<>();
                body.put("plan_id", m.getPlanId());
                body.put("member_id", m.getMemberId());
                body.put("distance_km", distKm);
                body.put("travel_minutes", tMin);
                if (m.getLateMinutes() != null) body.put("late_minutes", m.getLateMinutes());
                if (m.getWaitMinutes() != null) body.put("wait_minutes", m.getWaitMinutes());
                body.put("created_at", LocalDateTime.now().toString());

                String jsonPayload = objectMapper.writeValueAsString(body);

                // 실제 네트워크 요청 대신, 생성된 JSON을 로그로 출력
                log.info("[AI-PUSH-SIMULATION] Plan ID: {} / Member ID: {} / Payload: {}",
                        planId, m.getMemberId(), jsonPayload);

                ok++;

            } catch (Exception e) {
                log.error("[MOCK-PUSH] FAIL memberId={} reason={}", m.getMemberId(), e.toString());
            }
        }

        log.info("[MOCK-PUSH] done planId={} ok={}/{}", planId, ok, rows.size());
        return ok;
    }
}
