// src/main/java/com/oath/domain/metrics/service/MetricsPushService.java
package com.oath.domain.metrics.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oath.common.exception.Exception404;
import com.oath.domain.metrics.repository.ParticipantMetricsRepository;
import com.oath.domain.plan.domain.Plan;
import com.oath.domain.plan.repository.PlanJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final PlanJpaRepository planJpaRepository; // Plan 저장을 위해 추가
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    @Value("${ai.server.summary-url}")
    private String aiServerSummaryUrl;

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

                String resp = restTemplate.postForObject(aiServerUrl, entity, String.class);

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

    @Transactional("h2TransactionManager")
    public void fetchAndSaveSummary(Long planId) {
        log.info("[summary-fetch] AI 서버로부터 planId={}의 요약 보고서 수신을 시작합니다.", planId);
        try {
            // 1. GET 요청으로 AI 서버로부터 요약 보고서 수신
            String summary = restTemplate.getForObject(aiServerSummaryUrl, String.class, planId);
            log.info("[summary-fetch] 수신된 요약: {}", summary);

            // 2. Plan 엔티티를 조회하여 summary 필드 업데이트
            Plan plan = planJpaRepository.findById(planId)
                    .orElseThrow(() -> new Exception404("요약 보고서를 저장할 플랜을 찾을 수 없습니다: " + planId));

            plan.setSummary(summary);
            // @Transactional에 의해 메서드 종료 시 변경된 내용이 자동으로 DB에 반영(dirty-checking)

            log.info("[summary-fetch] planId={}에 요약 보고서 저장을 완료했습니다.", planId);

        } catch (HttpStatusCodeException e) {
            log.error("[summary-fetch] FAIL planId={} status={} body={}",
                    planId, e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("[summary-fetch] FAIL planId={} reason={}", planId, e.toString());
        }
    }
}
