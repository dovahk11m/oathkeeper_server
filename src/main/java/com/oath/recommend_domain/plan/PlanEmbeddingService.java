package com.oath.recommend_domain.plan;

import com.oath.common.exception.Exception404;
import com.oath.domain.plan.domain.Participant;
import com.oath.domain.plan.domain.Plan;
import com.oath.domain.plan.repository.ParticipantRepository;
import com.oath.recommend_domain.plan.request.EmbeddingRequest;
import com.oath.recommend_domain.plan.request.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class PlanEmbeddingService {

    private final PlanEmbeddingRepository planEmbeddingRepository;
    private final ParticipantRepository participantRepository;
    private final String apiKey;
    private final String embeddingEndpoint;
    private final String embeddingModel;

    public PlanEmbeddingService(PlanEmbeddingRepository planEmbeddingRepository,
                                ParticipantRepository participantRepository,
                                @Value("${ai.gemini.api-key}") String apiKey,
                                @Value("${ai.gemini.embedding-endpoint}") String embeddingEndpoint,
                                @Value("${ai.gemini.embedding-model}") String embeddingModel) {

        this.planEmbeddingRepository = planEmbeddingRepository;
        this.participantRepository = participantRepository;
        this.apiKey = apiKey;
        this.embeddingEndpoint = embeddingEndpoint;
        this.embeddingModel = embeddingModel;

        if (apiKey.equals("FAKE_AI_KEY"))
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n" +
                    "Gemini API Key가 할당되지 않아, 가짜 키가 주입되었습니다.\n" +
                    "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    @Transactional
    public void saveEmbedding(Plan plan) {
        try {
            PlanEmbedding planEmbedding = planEmbeddingRepository.save(
                    PlanEmbedding.builder()
                            .planId(plan.getId())
                            .planDatetime(plan.getPlanDatetime())
                            .status(plan.getStatus())
                            .placeLatitude(plan.getPlaceLatitude())
                            .placeLongitude(plan.getPlaceLongitude())
                            .build()
            );

            List<Participant> participants = participantRepository.findByPlanId(planEmbedding.getPlanId());
            float[] vector = getVector(PlanEmbedding.getnaturalLanguage(planEmbedding, plan, participants));
            updateEmbedding(planEmbedding.getId(), vector);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    @Transactional
    public void updateEmbedding(Long planEmbeddingId, float[] vector) {
        PlanEmbedding planEmbedding = planEmbeddingRepository.findById(planEmbeddingId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid planEmbedding Id:" + planEmbeddingId));

        planEmbedding.setEmbedding(vector);
        planEmbeddingRepository.save(planEmbedding);
    }

    // 임베딩 헬퍼 메서드
    private float[] getVector(String naturalLanguage) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        // HttpEntity에 요청 본문(embeddingRequest)과 헤더를 같이 담기
        HttpEntity<EmbeddingRequest> entity = new HttpEntity<>(EmbeddingRequest.buildEmbeddingRequest(naturalLanguage, embeddingModel), headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<EmbeddingResponse> response = restTemplate.exchange(embeddingEndpoint, HttpMethod.POST, entity, EmbeddingResponse.class);

        if (response.getBody() == null)
            throw new Exception404("응답 body가 비어있습니다.");

        return response.getBody().getValues();
    }

    // 임시로 넣어놓은 메서드 -> 모두 날려서 Supabase 최적화
    public void deleteAllInBatch() {
        planEmbeddingRepository.deleteAllInBatch();
    }
}
