// AI 서버와 통신하여 약속 요약 정보를 비동기로 생성하고 Plan 엔티티를 업데이트합니다.
package com.oath.domain.plan.service;

import com.oath.common.exception.Exception500;
import com.oath.domain.plan.SummaryStatus;
import com.oath.domain.plan.domain.Plan;
import com.oath.domain.plan.repository.PlanJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class AIService {

    private final RestTemplate restTemplate;
    private final PlanJpaRepository planJpaRepository;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    @Async
    @Transactional("h2TransactionManager")
    public CompletableFuture<Void> generateAndSaveSummary(Long planId) {
        Plan plan = planJpaRepository.findById(planId)
                .orElseThrow(() -> new Exception500("요약 생성 중 플랜을 찾을 수 없습니다."));

        try {
            String url = aiServerUrl + "/summarize/" + planId;
            String summary = restTemplate.getForObject(url, String.class);

            plan.setSummary(summary);
            plan.setSummaryStatus(SummaryStatus.COMPLETED);
            planJpaRepository.save(plan);

        } catch (Exception e) {
            plan.setSummaryStatus(SummaryStatus.FAILED);
            planJpaRepository.save(plan);
        }
        return CompletableFuture.completedFuture(null);
    }
}
