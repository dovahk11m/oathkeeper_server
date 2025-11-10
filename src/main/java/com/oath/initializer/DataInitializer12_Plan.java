package com.oath.initializer;

import com.oath.domain.members.domain.Member;
import com.oath.domain.members.repository.MemberRepository;
import com.oath.domain.plan.Status;
import com.oath.domain.plan.domain.Plan;
import com.oath.domain.plan.repository.PlanJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
@Profile("local")
@Order(12)
public class DataInitializer12_Plan implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PlanJpaRepository planJpaRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("👷‍♂️ 샘플 Plan 데이터 생성 시작");

        Member user1 = memberRepository.findByEmail("user1@test.com").orElseThrow();

        // 1. [H2 DB] 'Plan' 엔티티 생성 및 저장
        log.info("👷‍♂️ [H2] Plan 저장 시작");
        Plan plan = Plan.builder()
                .creatorMember(user1)
                .title("저녁에 치맥하실 분 (테스트)")
                .planDatetime(LocalDateTime.now().plusHours(5))
                .status(Status.PLANNING) // ⬅️ 우리가 만든 공용 Enum 사용
                .lateFineAmount(5000L)
                .build();

        // ⬇️ H2 DB의 'plan_tb'에 저장!
        Plan savedPlan = planJpaRepository.save(plan);
        log.info("👷‍♂️ [H2] Plan 저장 성공 (ID: {})", savedPlan.getId());

        log.info("👷‍♂️ 샘플 Plan 데이터 생성 완료");
    }
}
