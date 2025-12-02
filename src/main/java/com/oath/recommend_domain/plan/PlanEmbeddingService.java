package com.oath.recommend_domain.plan;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestTemplate;
import com.oath.common.exception.Exception404;
import com.oath.common.exception.Exception500;
import com.oath.domain.plan.domain.Plan;
import com.oath.domain.plan.service.PlanCoreService;
import com.oath.recommend_domain._common.dto.EmbeddingRequest;
import com.oath.recommend_domain._common.dto.EmbeddingResponse;
import com.oath.recommend_domain.plan.event_listener.PlanConfirmedEvent;

@Service
@Transactional("pgTransactionManager")
public class PlanEmbeddingService {

    private final PlanEmbeddingRepository planEmbeddingRepository;
    private final PlanCoreService planCoreService; // PlanCoreService 주입
    private final String apiKey;
    private final String embeddingEndpoint;
    private final String embeddingModel;

    public PlanEmbeddingService(PlanEmbeddingRepository planEmbeddingRepository,
            PlanCoreService planCoreService, // 의존성 변경
            @Value("${ai.gemini.api-key}") String apiKey,
            @Value("${ai.gemini.embedding-endpoint}") String embeddingEndpoint,
            @Value("${ai.gemini.embedding-model}") String embeddingModel) {

        this.planEmbeddingRepository = planEmbeddingRepository;
        this.planCoreService = planCoreService; // 의존성 변경
        this.apiKey = apiKey;
        this.embeddingEndpoint = embeddingEndpoint;
        this.embeddingModel = embeddingModel;

        if (apiKey.equals("FAKE_AI_KEY"))
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n"
                    + "Gemini API Key가 할당되지 않아, 가짜 키가 주입되었습니다.\n"
                    + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW) // 독립적인 새 트랜잭션으로 실행
    public void handlePlanConfirmation(PlanConfirmedEvent event) {

        Long planId = event.getPlanId();

        try {
            // PlanCoreService를 통해 Plan 조회
            Plan plan = planCoreService.getPlanById(planId);

            PlanEmbedding planEmbedding = saveOrUpdateEmbedding(plan, null);

            String naturalLanguage = PlanEmbedding.getNaturalLanguage(planEmbedding, plan);

            float[] vector = getVector(naturalLanguage);

            saveOrUpdateEmbedding(plan, vector);

        } catch (Exception e) {
            System.err.println("Plan 임베딩 생성 실패 (Plan ID: " + planId + "): " + e.getMessage());
        }
    }

    public PlanEmbedding saveOrUpdateEmbedding(Plan plan, float[] vector) {

        PlanEmbedding planEmbedding = planEmbeddingRepository.findByPlanId(plan.getId())
                .orElse(PlanEmbedding.builder().planId(plan.getId()).status(plan.getStatus())
                        .planDatetime(plan.getPlanDatetime()).placeLatitude(plan.getPlaceLatitude())
                        .placeLongitude(plan.getPlaceLongitude()).build());

        planEmbedding.setEmbedding(vector);
        return planEmbeddingRepository.save(planEmbedding);
    }

    // 임베딩 헬퍼 메서드
    private float[] getVector(String naturalLanguage) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        // HttpEntity에 요청 본문(embeddingRequest)과 헤더를 같이 담기
        HttpEntity<EmbeddingRequest> entity = new HttpEntity<>(
                EmbeddingRequest.buildEmbeddingRequest(naturalLanguage, embeddingModel), headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<EmbeddingResponse> response = restTemplate.exchange(embeddingEndpoint,
                HttpMethod.POST, entity, EmbeddingResponse.class);

        EmbeddingResponse body = response.getBody();
        if (body == null)
            throw new Exception404("응답 body가 비어있습니다.");

        return body.getValues();
    }

    // 트랜잭션 없이 실행 (외부 API 호출 포함)
    public List<Plan> findSimilarEmbeddings(Long planId, Long limit) {
        try {
            // 1단계: PlanEmbedding 조회 (짧은 트랜잭션)
            PlanEmbedding planEmbedding = findPlanEmbeddingById(planId);
            Plan plan = planCoreService.getPlanById(planId);
            String naturalLanguage = PlanEmbedding.getNaturalLanguage(planEmbedding, plan);

            // 2단계: 외부 API 호출 (트랜잭션 밖에서 실행!)
            float[] vector = getVector(naturalLanguage);

            // 3단계: 유사 Plan 조회 (새로운 짧은 트랜잭션)
            return findSimilarPlansWithVector(vector, limit);
        } catch (IllegalAccessException e) {
            throw new Exception500("서버 내부 오류가 발생했습니다. / 원인: " + e.getMessage());
        }
    }

    // 짧은 트랜잭션 1: PlanEmbedding 조회만
    @Transactional(value = "pgTransactionManager", readOnly = true)
    private PlanEmbedding findPlanEmbeddingById(Long planId) {
        return planEmbeddingRepository.findByPlanId(planId)
                .orElseThrow(() -> new Exception404("해당하는 플랜 임베딩을 찾을 수 없습니다."));
    }

    // 짧은 트랜잭션 2: 유사 Plan 조회만
    @Transactional(value = "pgTransactionManager", readOnly = true)
    private List<Plan> findSimilarPlansWithVector(float[] vector, Long limit) {
        List<Long> planIds = planEmbeddingRepository.findTopSimilarPlanEmbeddings(vector, limit)
                .stream().map(PlanEmbedding::getPlanId).toList();

        // PlanCoreService를 통해 Plan 목록 조회
        return planCoreService.listPlans(planIds);
    }

    // 임시로 넣어놓은 메서드 -> 모두 날려서 Supabase 최적화
    public void deleteAllInBatch() {
        planEmbeddingRepository.deleteAllInBatch();
    }
}
