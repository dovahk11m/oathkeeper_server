package com.oath.domain.metrics.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oath.common.exception.Exception404;
import com.oath.domain.metrics.dto.AiPlanSummaryResponse;
import com.oath.domain.metrics.repository.ParticipantMetricsRepository;
import com.oath.domain.plan.SummaryStatus;
import com.oath.domain.plan.domain.Plan;
import com.oath.domain.plan.repository.PlanJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsPushService {

    private final ParticipantMetricsRepository metricsRepo;
    private final PlanJpaRepository planJpaRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    @Value("${ai.server.endpoints.participant}")
    private String participantEndpoint;

    @Value("${ai.server.endpoints.plan-summary}")
    private String planSummaryEndpoint;

    public int pushPlan(Long planId) {
        var rows = metricsRepo.findAllByPlanId(planId);
        log.info("[metrics-push] planId={} rows={}", planId, rows.size());

        int ok = 0;
        for (var m : rows) {
            double distKm = (m.getDistanceKm() != null) ? m.getDistanceKm() : 0.0;
            int tMin = (m.getTravelMinutes() != null) ? m.getTravelMinutes() : 0;

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("plan_id", m.getPlanId());
            body.put("member_id", m.getMemberId());
            body.put("distance_km", distKm);
            body.put("travel_minutes", tMin);
            if (m.getLateMinutes() != null)
                body.put("late_minutes", m.getLateMinutes());
            if (m.getWaitMinutes() != null)
                body.put("wait_minutes", m.getWaitMinutes());
            body.put("created_at", LocalDateTime.now().toString());

            try {
                String json = objectMapper.writeValueAsString(body);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
                HttpEntity<String> entity = new HttpEntity<>(json, headers);

                String url = aiServerUrl + participantEndpoint;
                String resp = restTemplate.postForObject(url, entity, String.class);

                log.info("[metrics-push] sent memberId={} body={} resp={}", m.getMemberId(), json,
                        resp);
                ok++;

            } catch (HttpStatusCodeException e) {
                log.error("[metrics-push] FAIL memberId={} status={} body={}", m.getMemberId(),
                        e.getStatusCode(), e.getResponseBodyAsString());
            } catch (Exception e) {
                log.error("[metrics-push] FAIL memberId={} reason={}", m.getMemberId(),
                        e.toString());
            }
        }
        log.info("[metrics-push] done planId={} ok={}/{}", planId, ok, rows.size());
        return ok;
    }

    @Retryable(retryFor = {ResourceAccessException.class, HttpStatusCodeException.class},
            maxAttempts = 3, backoff = @Backoff(delay = 2000))
    @Transactional("h2TransactionManager")
    public void fetchAndSavePlanSummary(Long planId) {
        String url = aiServerUrl + planSummaryEndpoint.replace("{planId}", String.valueOf(planId));
        log.info("[plan-summary-fetch] AI 서버로 planId={}의 요약 보고서를 요청합니다. URL: {}", planId, url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        String jsonResponse = restTemplate.postForObject(url, entity, String.class);
        log.info("[plan-summary-fetch] 수신된 요약 JSON: {}", jsonResponse);

        String summaryText;
        try {
            AiPlanSummaryResponse response =
                    objectMapper.readValue(jsonResponse, AiPlanSummaryResponse.class);
            if (response.getSuccess() && response.getData() != null
                    && response.getData().getTextSummary() != null) {
                summaryText = response.getData().getTextSummary();
            } else {
                log.warn("[plan-summary-fetch] AI 서버 응답이 예상된 JSON 형식이 아닙니다. 원본을 저장합니다. response={}",
                        jsonResponse);
                summaryText = jsonResponse;
            }
        } catch (Exception e) {
            log.error("[plan-summary-fetch] AI 서버 응답 JSON 파싱에 실패했습니다. 원본을 저장합니다. response={}",
                    jsonResponse, e);
            summaryText = jsonResponse;
        }

        Plan plan = planJpaRepository.findById(planId)
                .orElseThrow(() -> new Exception404("요약 보고서를 저장할 플랜을 찾을 수 없습니다: " + planId));

        plan.setSummary(summaryText);
        plan.setSummaryStatus(SummaryStatus.COMPLETED);

        log.info("[plan-summary-fetch] planId={}에 요약 보고서 저장을 완료했습니다.", planId);
    }


    @Recover
    @Transactional("h2TransactionManager")
    public void recoverFetchAndSavePlanSummary(Exception e, Long planId) {
        log.error("[plan-summary-fetch][RECOVER] planId={}의 요약 보고서 수신에 최종 실패했습니다. 원인: {}", planId,
                e.getMessage());
        planJpaRepository.findById(planId).ifPresent(plan -> {
            plan.setSummary("AI 요약 생성에 실패했습니다: " + e.getMessage());
            plan.setSummaryStatus(SummaryStatus.FAILED);
            planJpaRepository.save(plan);
        });
    }
}
