// src/main/java/com/oath/domain/metrics/service/MetricsPushService.java
package com.oath.domain.metrics.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oath.domain.metrics.repository.ParticipantMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsPushService {

    private final ParticipantMetricsRepository metricsRepo;
    private final RestTemplate restTemplate;  // ✅ RestTemplate 사용
    private final ObjectMapper objectMapper;  // ✅ JSON 직렬화 보장

    public int pushPlan(Long planId) {
        var rows = metricsRepo.findAllByPlanId(planId);
        log.info("[metrics-push] planId={} rows={}", planId, rows.size());

        int ok = 0;

        for (var m : rows) {
            double distKm   = (m.getDistanceKm() != null) ? m.getDistanceKm() : 0.0;
            int    tMin     = (m.getTravelMinutes() != null) ? m.getTravelMinutes() : 0;

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("plan_id",        m.getPlanId());
            body.put("member_id",      m.getMemberId());
            body.put("distance_km",    distKm);
            body.put("travel_minutes", tMin);
            if (m.getLateMinutes() != null) body.put("late_minutes", m.getLateMinutes());
            if (m.getWaitMinutes() != null) body.put("wait_minutes", m.getWaitMinutes());
            body.put("created_at", LocalDateTime.now().toString());

            try {
                String json = objectMapper.writeValueAsString(body);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
                HttpEntity<String> entity = new HttpEntity<>(json, headers);

                // 🔸 디버그가 필요하면 /metrics/analyze_raw로 보내고 받은 json 확인
                String url = "http://localhost:8001/metrics/analyze"; // ← 최종 엔드포인트
                String resp = restTemplate.postForObject(url, entity, String.class);

                log.info("[metrics-push] sent memberId={} body={} resp={}", m.getMemberId(), json, resp);
                ok++;

            } catch (HttpStatusCodeException e) {
                log.error("[metrics-push] FAIL memberId={} status={} body={}",
                        m.getMemberId(), e.getStatusCode(), e.getResponseBodyAsString());
            } catch (Exception e) {
                log.error("[metrics-push] FAIL memberId={} reason={}", m.getMemberId(), e.toString());
            }
        }

        log.info("[metrics-push] done planId={} ok={}/{}", planId, ok, rows.size());
        return ok;
    }
}
