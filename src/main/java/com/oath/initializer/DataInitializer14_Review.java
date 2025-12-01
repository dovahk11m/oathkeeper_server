package com.oath.initializer;

import com.oath.domain.members.domain.Member;
import com.oath.domain.members.repository.MemberRepository;
import com.oath.domain.plan.domain.Plan;
import com.oath.domain.plan.repository.PlanJpaRepository;
import com.oath.domain.review.Review;
import com.oath.domain.review.ReviewJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
@Order(14)
public class DataInitializer14_Review {

    private final ReviewJpaRepository reviewJpaRepository;
    private final PlanJpaRepository planJpaRepository;
    private final MemberRepository memberRepository;

    @Transactional("h2TransactionManager")
    public void initialize() {
        log.info("Initializing 14: Review sample data");

        try {
            // planId=1 ("주말 등산 모임")은 COMPLETED 상태이므로, 후기 작성의 대상이 됨.
            Plan targetPlan = planJpaRepository.findById(1L).orElse(null);
            Member member2 = memberRepository.findById(2L).orElse(null);
            Member member3 = memberRepository.findById(3L).orElse(null);
            Member member4 = memberRepository.findById(4L).orElse(null);

            if (targetPlan == null || member2 == null || member3 == null || member4 == null) {
                log.warn("Review 샘플 데이터 초기화 실패: 필요한 Plan 또는 Member를 찾을 수 없습니다.");
                return;
            }

            Review review1 = Review.builder()
                    .plan(targetPlan)
                    .author(member2)
                    .title("별점 5점 드립니다!")
                    .content("경치가 정말 좋았어요! 다음에 또 가고 싶네요.")
                    // TODO: 사진 기능 구현 시 여기에 .photoUrls(List.of("url1.jpg", "url2.jpg")) 추가
                    .build();

            Review review2 = Review.builder()
                    .plan(targetPlan)
                    .author(member3)
                    .title("별점 4점!")
                    .content("날씨가 조금 아쉬웠지만 즐거운 산행이었습니다.")
                    // TODO: 사진 기능 구현 시 여기에 .photoUrls(List.of("url3.jpg")) 추가
                    .build();

            Review review3 = Review.builder()
                    .plan(targetPlan)
                    .author(member4)
                    .title("강력 추천합니다")
                    .content("초보자도 가기 좋은 코스였어요. 추천합니다!")
                    // TODO: 사진 기능 구현 시 여기에 .photoUrls(List.of()) 또는 필드 생략
                    .build();

            reviewJpaRepository.saveAll(List.of(review1, review2, review3));
            log.info("Initialized 3 reviews for planId=1");

        } catch (Exception e) {
            log.error("Error initializing review data", e);
        }
    }
}
