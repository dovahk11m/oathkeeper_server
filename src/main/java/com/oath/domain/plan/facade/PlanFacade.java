package com.oath.domain.plan.facade;

import com.oath.domain.place_tag_plan.plan_tag.PlanTag;
import com.oath.domain.place_tag_plan.plan_tag.PlanTagRepository;
import com.oath.domain.place_tag_plan.tag.Tag;
import com.oath.domain.place_tag_plan.tag.TagRepository;
import com.oath.domain.plan.Status;
import com.oath.domain.plan.domain.Plan;
import com.oath.domain.plan.repository.PlanJpaRepository;
import com.oath.domain.plan.request.PlanResponse;
import com.oath.domain.plan.service.PlanCoreService;
import com.oath.domain.plan.service.PlanParticipantService;
import com.oath.domain.plan.service.PlanTrackingService;
import com.oath.recommend_domain.plan.PlanEmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Transactional("h2TransactionManager") // Facade의 기본 트랜잭션 관리자 지정
public class PlanFacade {

    private final PlanCoreService planCoreService;
    private final PlanParticipantService planParticipantService;
    private final PlanTrackingService planTrackingService;
    private final PlanJpaRepository planJpaRepository;
    private final TagRepository tagRepository;
    private final PlanTagRepository planTagRepository;
    private final PlanEmbeddingService planEmbeddingService;

    @Transactional(readOnly = true)
    public List<PlanResponse.CreatePlan> listPlans(Long memberId) {
        List<Plan> plans = planCoreService.listPlans(memberId);
        return plans.stream()
                .map(plan -> PlanResponse.CreatePlan.of(plan))
                .collect(Collectors.toList());
    }

    // 이 메서드는 두 개의 다른 트랜잭션 관리자를 사용하는 서비스를 호출하므로,
    // 자체적인 트랜잭션을 시작하지 않고 각 서비스의 트랜잭션에 위임한다.
    public List<PlanResponse.CreatePlan> listRecommendPlans(Long currentPlanId, Long limit) {
        // planEmbeddingService는 내부적으로 "pgTransactionManager"를 사용한다.
        List<Plan> plans = planEmbeddingService.findSimilarEmbeddings(currentPlanId, limit);
        
        // DTO 변환 로직은 추가적인 DB 조회가 없으므로 트랜잭션이 필요 없다.
        return plans.stream()
                .map((plan) -> PlanResponse.CreatePlan.of(plan))
                .toList();
    }

    @Transactional(readOnly = true)
    public PlanResponse.CreatePlan getPlanById(Long planId) {
        Plan plan = planJpaRepository.findByIdWithParticipants(planId)
                .orElseThrow(() -> new com.oath.common.exception.Exception404("해당 플랜을 찾을 수 없습니다."));
        return PlanResponse.CreatePlan.of(plan);
    }

    public PlanResponse.CreatePlan createPlan(
            Long creatorMemberId,
            String title,
            LocalDateTime planDatetime,
            Status status,
            Long lateFineAmount
    ) {
        Plan plan = planCoreService.createPlan(
                creatorMemberId,
                title,
                planDatetime,
                status,
                lateFineAmount
        );
        Plan reloaded = planJpaRepository.findByIdWithParticipants(plan.getId())
                .orElse(plan);
        return PlanResponse.CreatePlan.of(reloaded);
    }

    public PlanResponse.CreatePlan updatePlan(
            Long planId,
            String title,
            LocalDateTime planDatetime,
            Status status,
            List<String> tags
    ) {
        Plan plan = planCoreService.updatePlan(
                planId,
                title,
                planDatetime,
                status
        );

        if (tags != null) {
            // 1. 요청으로 들어온 태그 이름들을 정규화
            Set<String> newTagNames = tags.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());

            // 2. 현재 Plan에 연결된 PlanTag 목록 조회
            Set<String> existingTagNames = plan.getPlanTags()
                    .stream()
                    .map(planTag -> planTag.getTag()
                            .getName())
                    .collect(Collectors.toSet());

            // 3. 삭제할 PlanTag 식별 및 삭제
            plan.getPlanTags()
                    .stream()
                    .filter(planTag -> !newTagNames.contains(planTag.getTag()
                            .getName()))
                    .forEach(planTagRepository::delete);

            // 4. 추가할 태그 식별 및 연결
            newTagNames.stream()
                    .filter(tagName -> !existingTagNames.contains(tagName))
                    .forEach(tagName -> {
                        // 태그를 찾거나 새로 생성
                        Tag tag = tagRepository.findByName(tagName)
                                .orElseGet(() -> tagRepository.save(Tag.builder()
                                        .name(tagName)
                                        .createdAt(LocalDateTime.now())
                                        .build()));

                        // PlanTag 생성 및 저장
                        PlanTag planTag = PlanTag.builder()
                                .plan(plan)
                                .tag(tag)
                                .createdAt(LocalDateTime.now())
                                .build();
                        planTagRepository.save(planTag);
                    });
        }

        // 변경된 Plan을 다시 로드하여 최신 상태 반영 (특히 planTags 컬렉션)
        Plan reloaded = planJpaRepository.findByIdWithParticipants(plan.getId())
                .orElse(plan);
        return PlanResponse.CreatePlan.of(reloaded);
    }

    public PlanResponse.CreatePlan confirmPlace(
            Long planId,
            String placeName,
            Point location
    ) {
        Plan plan = planCoreService.confirmPlace(
                planId,
                placeName,
                location
        );
        Plan reloaded = planJpaRepository.findByIdWithParticipants(plan.getId())
                .orElse(plan);
        return PlanResponse.CreatePlan.of(reloaded);
    }

    @Transactional(readOnly = true)
    public void validatePlanAccess(Long planId, Long memberId) {
        planCoreService.validatePlanAccess(planId, memberId);
    }

    @Transactional(readOnly = true)
    public void validatePlanCreator(Long planId, Long memberId) {
        planCoreService.validatePlanCreator(planId, memberId);
    }

    public void deletePlan(Long planId) {
        planCoreService.deletePlan(planId);
    }

    public Plan confirmFinalPlan(Long planId, Long requesterId) {
        return planCoreService.confirmFinalPlan(planId, requesterId);
    }

    @Transactional(readOnly = true)
    public Long calculateLateFine(Long participantId, Long requesterId) {
        return planTrackingService.calculateLateFine(participantId, requesterId);
    }

    public Plan completePlanManually(Long planId, Long requesterId) {
        return planTrackingService.completePlanManually(planId, requesterId);
    }

    public void markAllArrivedForTest(Long planId, Long requesterId) {
        planTrackingService.markAllArrivedForTest(planId, requesterId);
    }
}
