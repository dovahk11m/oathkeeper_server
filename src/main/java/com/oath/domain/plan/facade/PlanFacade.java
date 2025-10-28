package com.oath.domain.plan.facade;

import com.oath.domain.plan.*;
import com.oath.domain.plan.domain.Plan;
import com.oath.domain.plan.domain.Tag;
import com.oath.domain.plan.repository.PlanJpaRepository;
import com.oath.domain.plan.request.PlanResponse;
import com.oath.domain.plan.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PlanFacade {

    private final PlanService planService;
    private final PlanJpaRepository planJpaRepository;

    @Transactional(readOnly = true)
    public List<PlanResponse.CreatePlan> listPlans(Long memberId) {
        List<Plan> plans = planJpaRepository.findAllByCreatorOrParticipant(memberId);
        return plans.stream()
                .map(plan -> PlanResponse.CreatePlan.of(plan))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PlanResponse.CreatePlan getPlanById(Long planId) {
        Plan plan = planJpaRepository.findByIdWithParticipants(planId)
                .orElseThrow(() -> new com.oath.common.exception.Exception404("해당 플랜을 찾을 수 없습니다."));
        return PlanResponse.CreatePlan.of(plan);
    }

    @Transactional
    public PlanResponse.CreatePlan createPlan(Long creatorMemberId, String title, LocalDateTime planDatetime,
                                              Status status, Long lateFineAmount) {
        Plan plan = planService.createPlan(creatorMemberId, title, planDatetime, status, lateFineAmount);
        Plan reloaded = planJpaRepository.findByIdWithParticipants(plan.getId()).orElse(plan);
        return PlanResponse.CreatePlan.of(reloaded);
    }

    @Transactional
    public PlanResponse.CreatePlan updatePlan(Long planId, String title, LocalDateTime planDatetime,
                                              Status status, List<String> tags) {
        Plan plan = planService.updatePlan(planId, title, planDatetime, status);

        if (tags != null) {
            List<String> normalized = tags.stream()
                    .filter(s -> Objects.nonNull(s))
                    .map(s -> s.trim())
                    .filter(s -> !s.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());

            plan.clearTags();
            for (String tagName : normalized) {
                Tag tag = Tag.builder().tagName(tagName).build();
                plan.addTag(tag);
            }

            plan = planJpaRepository.save(plan);
        }

        Plan reloaded = planJpaRepository.findByIdWithParticipants(plan.getId()).orElse(plan);
        return PlanResponse.CreatePlan.of(reloaded);
    }

    @Transactional
    public PlanResponse.CreatePlan confirmPlace(Long planId, String placeName, Point location) {
        Plan plan = planService.confirmPlace(planId, placeName, location);
        Plan reloaded = planJpaRepository.findByIdWithParticipants(plan.getId()).orElse(plan);
        return PlanResponse.CreatePlan.of(reloaded);
    }
}

