// com/oath/domain/metrics/controller/MetricsTextController.java
package com.oath.domain.metrics.controller;

import com.oath.domain.plan.repository.ParticipantRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Profile("local") // 이 컨트롤러는 local 환경에서만 활성화
@Tag(name = "Metrics (내부 테스트용)", description = "통계 계산 및 AI 연동을 수동으로 테스트하기 위한 API")
@RestController
@RequestMapping("/api/plans/{planId}/metrics")
@RequiredArgsConstructor
public class MetricsTextController {

    private final WebClient aiClient; // baseUrl=http://localhost:8001
    private final ParticipantRepository participantRepo;

    @Operation(summary = "[4.1단계] AI 기반 텍스트 요약 생성 (옵션 포함)", description = "AI 서버에 스타일, 추가 노트 등의 옵션을 전달하여 커스텀된 텍스트 요약 보고서를 생성하도록 요청합니다.")
    @PostMapping("/summary/text")
    public ResponseEntity<String> getPromptedText(
            @Parameter(description = "플랜 ID") @PathVariable Long planId,
            @Parameter(description = "생성 모드 ('rules', 'prompt', 'llm')") @RequestParam(defaultValue = "prompt") String mode,
            @Parameter(description = "텍스트 스타일 (예: '친근하고 캐주얼하게')") @RequestParam(defaultValue = "친근하고 캐주얼하게") String style,
            @Parameter(description = "요약에 참고할 추가 노트") @RequestParam(defaultValue = "") String notes,
            @Parameter(description = "결과 재현을 위한 시드값 (선택)") @RequestParam(required = false) Integer seed) {

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
