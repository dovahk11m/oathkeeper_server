// com/oath/domain/metrics/controller/MetricsTextController.java
package com.oath.domain.metrics.controller;

import com.oath.domain.plan.repository.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/plans/{planId}/metrics")
@RequiredArgsConstructor
public class MetricsTextController {

    private final WebClient aiClient; // baseUrl=http://localhost:8001
    private final ParticipantRepository participantRepo;

    @PostMapping("/summary/text")
    public ResponseEntity<String> getPromptedText(@PathVariable Long planId,
                                                  @RequestParam(defaultValue = "prompt") String mode,
                                                  @RequestParam(defaultValue = "친근하고 캐주얼하게") String style,
                                                  @RequestParam(defaultValue = "") String notes,
                                                  @RequestParam(required = false) Integer seed) {
        // 1) 이름 조회
        List<ParticipantRepository.MemberIdName> pairs =
                participantRepo.findMemberIdNameByPlanId(planId);

        // 2) null 방지 + 키를 문자열로
        Map<String, String> nameMap =
                pairs.stream()
                        .filter(p -> p.getId() != null && p.getName() != null)
                        .collect(Collectors.toMap(
                                p -> p.getId().toString(),
                                ParticipantRepository.MemberIdName::getName,
                                (a, b) -> a, // 중복 키 발생 시 첫 번째 유지
                                LinkedHashMap::new
                        ));

        // 3) 바디 구성 (null 안전)
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mode", (mode == null ? "prompt" : mode));
        body.put("style", (style == null ? "" : style));
        body.put("notes", (notes == null ? "" : notes));
        if (seed != null) body.put("seed", seed);
        if (!nameMap.isEmpty()) body.put("name_map", nameMap);

        try {
            String resp = aiClient.post()
                    .uri("/metrics/report/{id}/text", planId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return ResponseEntity.ok(resp);
        } catch (WebClientResponseException e) {
            // 422 등 상세 바디 로깅
            String detail = e.getResponseBodyAsString();
            return ResponseEntity.status(e.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"" + e.getStatusCode() + "\",\"detail\":" + (detail == null ? "\"\"" : detail) + "}");
        }
    }
}
