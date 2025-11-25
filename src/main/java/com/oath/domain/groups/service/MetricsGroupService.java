package com.oath.domain.groups.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oath.common.exception.Exception404;
import com.oath.domain.groups.Group;
import com.oath.domain.groups.SummaryStatus;
import com.oath.domain.groups.repository.GroupRepository;
import com.oath.domain.metrics.dto.AiGroupSummaryRequest;
import com.oath.domain.metrics.dto.AiGroupSummaryResponse;
import com.oath.domain.plan.repository.PlanJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MetricsGroupService {

    private final GroupRepository groupRepository;
    private final PlanJpaRepository planJpaRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient; // HttpClient 주입

    @Value("${ai.server.url}")
    private String aiServerBaseUrl;

    @Async
    @Transactional
    public void requestGroupSummary(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new Exception404("Group not found with id: " + groupId)); // Exception404로 변경

        if (group.getSummaryStatus() == SummaryStatus.PENDING) {
            log.info("Group {} summary is already PENDING. Skipping new request.", groupId);
            return;
        }

        group.updateSummary(null, SummaryStatus.PENDING);
        groupRepository.save(group);

        List<Long> completedPlanIds = planJpaRepository.findCompletedPlanIdsByGroupId(groupId);

        if (completedPlanIds.isEmpty()) {
            group.updateSummary("요약할 완료된 약속이 없습니다.", SummaryStatus.FAILED);
            groupRepository.save(group);
            log.warn("Group {} has no completed plans to summarize. Status set to FAILED.", groupId);
            return;
        }

        AiGroupSummaryRequest requestBody = AiGroupSummaryRequest.builder()
                .groupId(groupId)
                .planIds(completedPlanIds)
                .build();

        log.info("Requesting AI summary for Group {}. Plan IDs: {}", groupId, completedPlanIds);
        log.debug("aiServerBaseUrl: {}", aiServerBaseUrl);

        try {
            String requestJson = objectMapper.writeValueAsString(requestBody);
            log.debug("AI Summary Request JSON for Group {}: {}", groupId, requestJson);

            String requestUri = aiServerBaseUrl + "/metrics/group/summary";
            log.debug("Final AI Summary Request URI for Group {}: {}", groupId, requestUri);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(requestUri))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .timeout(Duration.ofSeconds(35))
                    .build();

            // 주입받은 httpClient 사용
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("AI Summary Response for Group {}. Status: {}, Body: {}", groupId, response.statusCode(), response.body());

            if (response.statusCode() == 200) {
                AiGroupSummaryResponse aiResponse = objectMapper.readValue(response.body(), AiGroupSummaryResponse.class);

                if (aiResponse.getSuccess() != null && !aiResponse.getSuccess()) {
                    String errorMessage = aiResponse.getMessage() != null ? aiResponse.getMessage() : "AI 요약 생성 실패 (AI 서버 응답: success=false)";
                    group.updateSummary(errorMessage, SummaryStatus.FAILED);
                    groupRepository.save(group);
                    log.error("AI summary for Group {} FAILED. AI server reported success=false. Message: {}", groupId, errorMessage);
                } else {
                    group.updateSummary(aiResponse.getData().getTextSummary(), SummaryStatus.COMPLETED);
                    groupRepository.save(group);
                    log.info("AI summary for Group {} COMPLETED. Summary: {}", groupId, aiResponse.getData().getTextSummary());

                    if (aiResponse.getWarnings() != null && !aiResponse.getWarnings().isEmpty()) {
                        log.warn("AI summary for Group {} completed with warnings: {}", groupId, aiResponse.getWarnings());
                    }
                }
            } else if (response.statusCode() == 409) {
                String errorMessage = "AI 요약 생성 실패: 분석 가능한 데이터가 없습니다.";
                try {
                    AiGroupSummaryResponse aiResponse = objectMapper.readValue(response.body(), AiGroupSummaryResponse.class);
                    if (aiResponse.getMessage() != null) {
                        errorMessage = "AI 요약 생성 실패: " + aiResponse.getMessage();
                    }
                } catch (IOException jsonEx) {
                    log.warn("Failed to parse 409 response body for Group {}: {}", groupId, jsonEx.getMessage());
                }
                group.updateSummary(errorMessage, SummaryStatus.FAILED);
                groupRepository.save(group);
                log.error("AI summary for Group {} FAILED. HTTP Status 409. Message: {}", groupId, errorMessage);
            } else {
                String errorMessage = "AI 요약 생성에 실패했습니다. (HTTP Status: " + response.statusCode() + ")";
                group.updateSummary(errorMessage, SummaryStatus.FAILED);
                groupRepository.save(group);
                log.error("AI summary for Group {} FAILED. HTTP Status: {}, Response Body: {}", groupId, response.statusCode(), response.body());
            }
        } catch (IOException e) {
            group.updateSummary("AI 요약 생성 중 통신 오류 발생: " + e.getMessage(), SummaryStatus.FAILED);
            groupRepository.save(group);
            log.error("Failed to get AI summary for Group {} due to IOException: {}", groupId, e.getMessage(), e);
        } catch (InterruptedException e) {
            group.updateSummary("AI 요약 생성 중 스레드 인터럽트 발생: " + e.getMessage(), SummaryStatus.FAILED);
            groupRepository.save(group);
            log.error("Failed to get AI summary for Group {} due to InterruptedException: {}", groupId, e.getMessage(), e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            group.updateSummary("AI 요약 생성 중 예상치 못한 오류 발생: " + e.getMessage(), SummaryStatus.FAILED);
            groupRepository.save(group);
            log.error("Failed to get AI summary for Group {} due to unexpected error: {}", groupId, e.getMessage(), e);
        }
    }
}
