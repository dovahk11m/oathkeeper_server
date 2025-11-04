package com.oath.recommend_domain.plan;

import com.oath.common.exception.Exception404;
import com.oath.domain.plan.domain.Participant;
import com.oath.domain.plan.domain.Plan;
import com.oath.domain.plan.repository.ParticipantRepository;
import com.oath.domain.plan.repository.PlanJpaRepository;
import com.oath.recommend_domain.plan.event_listener.PlanConfirmedEvent;
import com.oath.recommend_domain.plan.request.EmbeddingRequest;
import com.oath.recommend_domain.plan.request.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class PlanEmbeddingService {

    private final PlanEmbeddingRepository planEmbeddingRepository;
    private final ParticipantRepository participantRepository;
    private final PlanJpaRepository planJpaRepository;
    private final String apiKey;
    private final String embeddingEndpoint;
    private final String embeddingModel;

    public PlanEmbeddingService(PlanEmbeddingRepository planEmbeddingRepository,
                                ParticipantRepository participantRepository,
                                PlanJpaRepository planJpaRepository,
                                @Value("${ai.gemini.api-key}") String apiKey,
                                @Value("${ai.gemini.embedding-endpoint}") String embeddingEndpoint,
                                @Value("${ai.gemini.embedding-model}") String embeddingModel) {

        this.planEmbeddingRepository = planEmbeddingRepository;
        this.participantRepository = participantRepository;
        this.planJpaRepository = planJpaRepository;
        this.apiKey = apiKey;
        this.embeddingEndpoint = embeddingEndpoint;
        this.embeddingModel = embeddingModel;

        if (apiKey.equals("FAKE_AI_KEY"))
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n" +
                    "Gemini API Key가 할당되지 않아, 가짜 키가 주입되었습니다.\n" +
                    "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePlanConfirmation(PlanConfirmedEvent event) {

        Long planId = event.getPlanId();

        try {
            Plan plan = planJpaRepository.findByIdWithParticipants(planId)
                    .orElseThrow(() -> new Exception404("이벤트 리스너: 해당하는 플랜을 찾을 수 없습니다."));

            List<Participant> participants = participantRepository.findByPlanId(planId);

            PlanEmbedding planEmbedding = saveOrUpdateEmbedding(
                    plan, null
            );

            String naturalLanguage = PlanEmbedding.getNaturalLanguage(planEmbedding, plan);

            float[] vector = getVector(naturalLanguage);

            saveOrUpdateEmbedding(plan, vector);

        } catch (Exception e) {
            System.err.println("Plan 임베딩 생성 실패 (Plan ID: " + planId + "): " + e.getMessage());
        }
    }

    @Transactional
    public PlanEmbedding saveOrUpdateEmbedding(Plan plan, float[] vector) {

        PlanEmbedding planEmbedding = planEmbeddingRepository.findByPlanId(plan.getId())
                .orElse(PlanEmbedding.builder()
                        .planId(plan.getId())
                        .status(plan.getStatus())
                        .planDatetime(plan.getPlanDatetime())
                        .placeLatitude(plan.getPlaceLatitude())
                        .placeLongitude(plan.getPlaceLongitude())
                        .build()
                );

        planEmbedding.setEmbedding(vector);
        return planEmbeddingRepository.save(planEmbedding);
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

//    public void findSimilarEmbedding(Long limit) {
//
//        String naturalLanguage = PlanEmbedding.getNaturalLanguage(, );
//        planEmbeddingRepository.findTopSimilarPlans(getVector(naturalLanguage), limit);
//    }

    // 임시로 넣어놓은 메서드 -> 모두 날려서 Supabase 최적화
    public void deleteAllInBatch() {
        planEmbeddingRepository.deleteAllInBatch();
    }
}
