// com/oath/domain/metrics/controller/MetricsSummaryController.java
package com.oath.domain.metrics.controller;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.oath.domain.plan.repository.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/plans/{planId}/metrics")
@RequiredArgsConstructor
public class MetricsSummaryController {

    private final WebClient aiClient;                 // baseUrl=http://localhost:8001 (기존 WebClientConfig)
    private final ParticipantRepository participantRepo;
    private final ObjectMapper om = new ObjectMapper();

    /** 요약 JSON 프록시 + member_name 주입 */
    @GetMapping("/summary")
    public ResponseEntity<String> getSummary(@PathVariable Long planId) throws Exception {
        // 1) FastAPI에서 원본 요약 가져오기
        String raw = aiClient.get()
                .uri("/metrics/report/{id}", planId)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // 2) plan 참가자 id->이름 맵 작성
        Map<Long, String> nameMap = participantRepo.findMemberIdNameByPlanId(planId)
                .stream()
                .collect(Collectors.toMap(ParticipantRepository.MemberIdName::getId,
                        ParticipantRepository.MemberIdName::getName));

        // 3) JSON 수정: members[].member_name, highlights의 *_member_name
        JsonNode root = om.readTree(raw);
        ObjectNode summary = (ObjectNode) root.get("summary");

        // members
        ArrayNode members = (ArrayNode) summary.get("members");
        for (JsonNode m : members) {
            long mid = m.get("member_id").asLong();
            ((ObjectNode) m).put("member_name", nameMap.getOrDefault(mid, "회원#" + mid));
        }

        // highlights
        ObjectNode hi = (ObjectNode) summary.get("highlights");
        addName(hi, "top_distance_member_id", nameMap);
        addName(hi, "top_minutes_member_id",  nameMap);
        addName(hi, "top_late_member_id",     nameMap);
        addName(hi, "top_wait_member_id",     nameMap);

        return ResponseEntity.ok(om.writeValueAsString(root));
    }

    private static void addName(ObjectNode hi, String key, Map<Long,String> nameMap) {
        if (hi != null && hi.hasNonNull(key)) {
            long id = hi.get(key).asLong();
            hi.put(key.replace("_id", "_name"), nameMap.getOrDefault(id, "회원#" + id));
        }
    }

    /** (옵션) 자연어 텍스트는 FastAPI 그대로 프록시 */
    @GetMapping("/summary/text")
    public ResponseEntity<String> getSummaryText(@PathVariable Long planId) {
        String json = aiClient.get()
                .uri("/metrics/report/{id}/text", planId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return ResponseEntity.ok(json);
    }
}
