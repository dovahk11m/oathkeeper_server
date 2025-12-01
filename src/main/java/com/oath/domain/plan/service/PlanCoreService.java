package com.oath.domain.plan.service;

import com.oath.common.exception.Exception400;
import com.oath.common.exception.Exception403;
import com.oath.common.exception.Exception404;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.repository.MemberRepository;
import com.oath.domain.plan.Status;
import com.oath.domain.plan.domain.Participant;
import com.oath.domain.plan.domain.Plan;
import com.oath.domain.plan.repository.PlanJpaRepository;
import com.oath.domain.plan.repository.ParticipantRepository;
import com.oath.recommend_domain.plan.event_listener.PlanConfirmedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional("h2TransactionManager")
public class PlanCoreService {

    private final PlanJpaRepository planJpaRepository;
    private final MemberRepository memberRepository;
    private final ParticipantRepository participantRepository;
    private final ApplicationEventPublisher eventPublisher;

    // 플랜 조회
    @Transactional(readOnly = true)
    public Plan getPlanById(Long planId) {
        return planJpaRepository.findById(planId)
                .orElseThrow(() -> new Exception404("해당 플랜을 찾을 수 없습니다."));
    }

    // 여러 플랜 조회
    @Transactional(readOnly = true)
    public List<Plan> listPlans(List<Long> planIds) {
        return planJpaRepository.findAllByIdInWithParticipants(planIds);
    }

    // 플랜 접근 권한 검증 (생성자 또는 참가자만 접근 가능)
    @Transactional(readOnly = true)
    public void validatePlanAccess(
            Long planId,
            Long memberId
    ) {
        Plan plan = planJpaRepository.findByIdWithParticipants(planId)
                .orElseThrow(() -> new Exception404("해당 플랜을 찾을 수 없습니다."));
        boolean isCreator = plan.getCreatorMember()
                .getId()
                .equals(memberId);
        boolean isParticipant = plan.getParticipants()
                .stream()
                .anyMatch(p -> p.getMember()
                        .getId()
                        .equals(memberId));

        if (!isCreator && !isParticipant) {
            throw new Exception403("이 플랜에 접근할 권한이 없습니다.");
        }
    }

    // 플랜 생성자 권한 검증 (생성자만 가능)
    @Transactional(readOnly = true)
    public void validatePlanCreator(
            Long planId,
            Long memberId
    ) {
        Plan plan = getPlanById(planId);
        if (!plan.getCreatorMember()
                .getId()
                .equals(memberId)) {
            throw new Exception403("플랜 생성자만 수정/삭제할 수 있습니다.");
        }
    }

    // 플랜 생성
    public Plan createPlan(
            Long creatorMemberId,
            String title,
            LocalDateTime planDatetime,
            Status status,
            Long lateFineAmount
    ) {
        Member creator = memberRepository.findById(creatorMemberId)
                .orElseThrow(() -> new Exception404("해당 멤버를 찾을 수 없습니다."));
        Plan plan = Plan.builder()
                .creatorMember(creator)
                .title(title)
                .planDatetime(planDatetime)
                .status(status)
                .lateFineAmount(lateFineAmount)
                .build();

        return planJpaRepository.save(plan);
    }

    // 플랜 수정
    public Plan updatePlan(
            Long planId,
            String title,
            LocalDateTime planDatetime,
            Status status
    ) {
        Plan plan = getPlanById(planId);
        plan.update(
                title,
                planDatetime,
                status
        );

        return planJpaRepository.save(plan);
    }

    // 플랜 삭제
    public void deletePlan(Long planId) {
        if (!planJpaRepository.existsById(planId)) {
            throw new Exception404("해당 플랜을 찾을 수 없습니다.");
        }
        planJpaRepository.deleteById(planId);
    }

    // 플랜 목록 조회 (본인이 생성하거나 참여한 플랜만)
    @Transactional(readOnly = true)
    public List<Plan> listPlans(Long memberId) {
        return planJpaRepository.findAllByCreatorOrParticipant(memberId);
    }

    // 그룹 ID와 상태로 플랜 목록 조회 (페이징)
    @Transactional(readOnly = true)
    public Page<Plan> getPlansByGroupAndStatus(Long groupId, Status status, Pageable pageable) {
        return planJpaRepository.findByGroupIdAndStatus(groupId, status, pageable);
    }

    // 장소 확정
    public Plan confirmPlace(
            Long planId,
            String placeName,
            Point location
    ) {
        Plan plan = getPlanById(planId);
        plan.confirmPlace(
                placeName,
                location
        );

        return planJpaRepository.save(plan);
    }

    public Plan confirmFinalPlan(
            Long planId,
            Long requesterId
    ) {

        validatePlanCreator(
                planId,
                requesterId
        );

        Plan plan = getPlanById(planId);

        if (plan.getStatus() == Status.CONFIRMED || plan.getStatus() == Status.COMPLETED) {
            throw new Exception400("이미 확정되거나 완료된 약속입니다.");
        }

        if (plan.getPlaceName() == null || plan.getPlaceLatitude() == null) {
            throw new Exception400("장소가 아직 확정되지 않았습니다.");
        }

        List<Participant> participants = participantRepository.findByPlanId(planId);
        if (participants.isEmpty()) {
            throw new Exception400("참여자가 한 명도 없는 약속은 확정할 수 없습니다.");
        }

        plan.update(
                plan.getTitle(),
                plan.getPlanDatetime(),
                Status.CONFIRMED
        );
        Plan savedPlan = planJpaRepository.save(plan);

        eventPublisher.publishEvent(new PlanConfirmedEvent(savedPlan.getId())); //

        return savedPlan;
    }
}
