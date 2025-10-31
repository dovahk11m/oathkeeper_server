package com.oath.initializer;

import com.oath.domain.members.domain.Member;
import com.oath.domain.members.repository.MemberRepository;
import com.oath.domain.plan.Status;
import com.oath.domain.plan.domain.Plan;
import com.oath.domain.plan.repository.PlanJpaRepository;
import com.oath.recommend_domain.plan.PlanEmbedding;
import com.oath.recommend_domain.plan.PlanEmbeddingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("local")
@Order(4)
public class DataInitializer5_Plan implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PlanJpaRepository planJpaRepository;
    private final PlanEmbeddingRepository planEmbeddingRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("샘플 Plan 데이터를 생성합니다...");

        Member user1 = memberRepository.findByEmail("user1@test.com").orElseThrow();

        // 1. [H2 DB] 'Plan' 엔티티 생성 및 저장
        log.info("[H2] Plan 저장을 시도합니다...");
        Plan plan = Plan.builder()
                .creatorMember(user1)
                .title("저녁에 치맥하실 분 (테스트)")
                .planDatetime(LocalDateTime.now().plusHours(5))
                .status(Status.PLANNING) // ⬅️ 우리가 만든 공용 Enum 사용
                .lateFineAmount(5000L)
                .build();

        // ⬇️ H2 DB의 'plan_tb'에 저장!
        Plan savedPlan = planJpaRepository.save(plan);
        log.info("✅ [H2] Plan 저장 성공! (ID: {})", savedPlan.getId());


        // 2. [PG DB] 'PlanEmbedding' 엔티티 생성
        log.info("[PG] PlanEmbedding 저장을 시도합니다...");

        // (임시) AI가 만들어준 가짜 벡터
        float[] fakeEmbedding = new float[768];
        fakeEmbedding[0] = 0.1f; // (테스트용 가짜 값)

        PlanEmbedding embedding = PlanEmbedding.builder()
                .planId(savedPlan.getId()) // ⬅️ [핵심!] H2 DB의 Plan ID를 링크
                .embedding(fakeEmbedding)
                // 캐시 데이터 복사
                .planDatetime(savedPlan.getPlanDatetime())
                .status(savedPlan.getStatus())
                .placeLatitude(savedPlan.getPlaceLatitude())
                .placeLongitude(savedPlan.getPlaceLongitude())
                .build();

        // ⬇️ PG DB의 'plan_embeddings'에 저장!
        // (이 시점엔 ddl-auto가 이미 끝나서 테이블이 존재함)
        planEmbeddingRepository.save(embedding);
        log.info("✅ [PG] PlanEmbedding 저장 성공! (Plan ID: {})", savedPlan.getId());

        log.info("샘플 Plan 데이터 생성이 완료되었습니다.");
    }
}
