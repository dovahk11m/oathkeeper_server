package com.oath.domain.plan;

import com.oath.common.exception.Exception404;
import com.oath.common.exception.Exception400;
import com.oath.domain.members.Member;
import com.oath.domain.plan.request.PlanMemberResponse;
import com.oath.domain.plan.request.PlanResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanJpaRepository planJpaRepository;
    private final PlanMemberRepository planMemberRepository;
    private final MemberRepository memberRepository;

    // 기존 엔티티 반환 메서드들은 내부 로직에서 사용
    // 플랜 조회 (엔티티)
    public Plan getPlanById(Long planId) {
        return planJpaRepository.findById(planId).orElseThrow(() -> new Exception404("해당 플랜을 찾을 수 없습니다."));
    }

    // 플랜 생성 (엔티티)
    @Transactional
    public Plan createPlan(Long creatorMemberId, String title, LocalDateTime planDatetime, Status status, Long lateFineAmount) {
        Member creator = memberRepository.findById(creatorMemberId).orElseThrow(() -> new Exception404("해당 멤버를 찾을 수 없습니다."));
        Plan plan = new Plan(creator, title, planDatetime, status, lateFineAmount);

        return planJpaRepository.save(plan);
    }

    // 플랜 수정 (엔티티)
    @Transactional
    public Plan updatePlan(Long planId, String title, LocalDateTime planDatetime, Status status) {
        Plan plan = getPlanById(planId);
        plan.update(title, planDatetime, status);

        return planJpaRepository.save(plan);
    }

    // 플랜 삭제
    @Transactional
    public void deletePlan(Long planId) {
        if (!planJpaRepository.existsById(planId)) {
            throw new Exception404("해당 플랜을 찾을 수 없습니다.");
        }
        planJpaRepository.deleteById(planId);
    }

    // 플랜 목록 조회 (엔티티)
    public List<Plan> listPlans() {
        return planJpaRepository.findAll();
    }

    // 참가자 추가 (엔티티)
    @Transactional
    public PlanMember addParticipant(Long planId, Long memberId) {
        Plan plan = getPlanById(planId);
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new Exception404("해당 멤버를 찾을 수 없습니다."));
        PlanMember participant = new PlanMember();
        participant.setPlan(plan);
        participant.setMember(member);
        participant.setParticipantStatus(ParticipantStatus.PENDING);
        PlanMember saved = planMemberRepository.save(participant);
        plan.getParticipants().add(saved);

        return saved;
    }

    // 참가자 삭제
    @Transactional
    public void removeParticipant(Long participantId) {
        if (!planMemberRepository.existsById(participantId)) {
            throw new Exception404("해당 참가자를 찾을 수 없습니다.");
        }
        planMemberRepository.deleteById(participantId);
    }

    // 참가자 상태 변경 (엔티티)
    @Transactional
    public PlanMember changeParticipantStatus(Long participantId, ParticipantStatus status) {
        PlanMember pm = planMemberRepository.findById(participantId).orElseThrow(() -> new Exception404("참가자를 찾을 수 없습니다."));
        pm.setParticipantStatus(status);

        return planMemberRepository.save(pm);
    }

    // 참가자 조회 (엔티티)
    public List<PlanMember> getParticipants(Long planId) {
        if (!planJpaRepository.existsById(planId)) {
            throw new Exception404("해당 플랜을 찾을 수 없습니다.");
        }
        return planMemberRepository.findByPlanId(planId);
    }

    // 출발 시간 기록 (엔티티)
    @Transactional
    public PlanMember recordDeparture(Long participantId, LocalDateTime actualDeparture) {
        PlanMember pm = planMemberRepository.findById(participantId).orElseThrow(() -> new Exception404("참가자를 찾을 수 없습니다."));
        pm.setActualDepartureTime(actualDeparture != null ? actualDeparture : LocalDateTime.now());

        return planMemberRepository.save(pm);
    }

    // 도착 시간 기록 (엔티티)
    @Transactional
    public PlanMember recordArrival(Long participantId, LocalDateTime actualArrival) {
        PlanMember pm = planMemberRepository.findById(participantId).orElseThrow(() -> new Exception404("참가자를 찾을 수 없습니다."));
        pm.setActualArrivalTime(actualArrival != null ? actualArrival : LocalDateTime.now());
        LocalDateTime planTime = pm.getPlan().getPlanDatetime();
        if (planTime == null) {
            throw new Exception400("플랜의 약속 시간이 설정되어 있지 않습니다.");
        }
        long minutesDiff = ChronoUnit.MINUTES.between(planTime, pm.getActualArrivalTime());
        pm.setTimeBurdenMinutes((int) minutesDiff);

        return planMemberRepository.save(pm);
    }

    // 예상 출발 시간 제안 (엔티티)
    @Transactional
    public PlanMember suggestExpectedDeparture(Long participantId, Integer expectedTravelTimeMinutes) {
        PlanMember pm = planMemberRepository.findById(participantId).orElseThrow(() -> new Exception404("참가자를 찾을 수 없습니다."));
        pm.setExpectedTravelTimeMinutes(expectedTravelTimeMinutes);
        LocalDateTime planTime = pm.getPlan().getPlanDatetime();
        if (planTime == null) {
            throw new Exception400("플랜의 약속 시간이 설정되어 있지 않습니다.");
        }
        LocalDateTime expectedDeparture = planTime.minusMinutes(expectedTravelTimeMinutes != null ? expectedTravelTimeMinutes : 0);
        pm.setExpectedDepartureTime(expectedDeparture);

        return planMemberRepository.save(pm);
    }

    // 지각 벌금 계산
    public Long calculateLateFine(Long participantId) {
        PlanMember pm = planMemberRepository.findById(participantId).orElseThrow(() -> new Exception404("참가자를 찾을 수 없습니다."));
        Integer burden = pm.getTimeBurdenMinutes();
        if (burden != null && burden > 0) {
            Long fine = pm.getPlan().getLateFineAmount();
            return fine != null ? fine : 0L;
        }

        return 0L;
    }

    // 장소 확정 (엔티티)
    @Transactional
    public Plan confirmPlace(Long planId, String placeName, Double latitude, Double longitude) {
        Plan plan = getPlanById(planId);
        plan.confirmPlace(placeName, latitude, longitude);

        return planJpaRepository.save(plan);
    }

    // ------------------ DTO 반환용 래퍼 메서드 ------------------

    @Transactional(readOnly = true)
    public List<PlanResponse.CreatePlan> listPlansDto() {
        List<Plan> plans = planJpaRepository.findAllWithParticipants();
        return plans.stream().map(PlanResponse.CreatePlan::of).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PlanResponse.CreatePlan getPlanDtoById(Long planId) {
        Plan plan = planJpaRepository.findByIdWithParticipants(planId).orElseThrow(() -> new Exception404("해당 플랜을 찾을 수 없습니다."));
        return PlanResponse.CreatePlan.of(plan);
    }

    @Transactional
    public PlanResponse.CreatePlan createPlanDto(Long creatorMemberId, String title, LocalDateTime planDatetime, Status status, Long lateFineAmount) {
        Plan plan = createPlan(creatorMemberId, title, planDatetime, status, lateFineAmount);
        Plan reloaded = planJpaRepository.findByIdWithParticipants(plan.getId()).orElse(plan);
        return PlanResponse.CreatePlan.of(reloaded);
    }

    @Transactional
    public PlanResponse.CreatePlan updatePlanDto(Long planId, String title, LocalDateTime planDatetime, Status status) {
        Plan plan = updatePlan(planId, title, planDatetime, status);
        Plan reloaded = planJpaRepository.findByIdWithParticipants(plan.getId()).orElse(plan);
        return PlanResponse.CreatePlan.of(reloaded);
    }

    @Transactional
    public PlanResponse.CreatePlan confirmPlaceDto(Long planId, String placeName, Double latitude, Double longitude) {
        Plan plan = confirmPlace(planId, placeName, latitude, longitude);
        Plan reloaded = planJpaRepository.findByIdWithParticipants(plan.getId()).orElse(plan);
        return PlanResponse.CreatePlan.of(reloaded);
    }

    @Transactional
    public PlanMemberResponse addParticipantDto(Long planId, Long memberId) {
        PlanMember pm = addParticipant(planId, memberId);
        return PlanMemberResponse.of(pm);
    }

    @Transactional
    public PlanMemberResponse changeParticipantStatusDto(Long participantId, ParticipantStatus status) {
        PlanMember pm = changeParticipantStatus(participantId, status);
        return PlanMemberResponse.of(pm);
    }

    @Transactional(readOnly = true)
    public List<PlanMemberResponse> getParticipantsDto(Long planId) {
        List<PlanMember> list = getParticipants(planId);
        return list.stream().map(PlanMemberResponse::of).collect(Collectors.toList());
    }

    @Transactional
    public PlanMemberResponse recordDepartureDto(Long participantId, LocalDateTime actualDeparture) {
        PlanMember pm = recordDeparture(participantId, actualDeparture);
        return PlanMemberResponse.of(pm);
    }

    @Transactional
    public PlanMemberResponse recordArrivalDto(Long participantId, LocalDateTime actualArrival) {
        PlanMember pm = recordArrival(participantId, actualArrival);
        return PlanMemberResponse.of(pm);
    }

    @Transactional
    public PlanMemberResponse suggestExpectedDepartureDto(Long participantId, Integer expectedTravelTimeMinutes) {
        PlanMember pm = suggestExpectedDeparture(participantId, expectedTravelTimeMinutes);
        return PlanMemberResponse.of(pm);
    }

}
