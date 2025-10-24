package com.oath.domain.plan;

import com.oath.common.exception.Exception404;
import com.oath.common.exception.Exception400;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.repository.MemberRepository;
import com.oath.domain.plan.request.ParticipantResponse;
import com.oath.domain.plan.request.PlanResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.geo.Point;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanJpaRepository planJpaRepository;
    private final ParticipantRepository participantRepository;
    private final MemberRepository memberRepository;


    // 플랜 조회
    public Plan getPlanById(Long planId) {
        return planJpaRepository.findById(planId).orElseThrow(() -> new Exception404("해당 플랜을 찾을 수 없습니다."));
    }

    // 플랜 생성
    @Transactional
    public Plan createPlan(Long creatorMemberId, String title, LocalDateTime planDatetime, Status status, Long lateFineAmount) {
        Member creator = memberRepository.findById(creatorMemberId).orElseThrow(() -> new Exception404("해당 멤버를 찾을 수 없습니다."));
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

    // 플랜 목록 조회
    public List<Plan> listPlans() {
        return planJpaRepository.findAll();
    }

    // 참가자 추가
    @Transactional
    public Participant addParticipant(Long planId, Long memberId) {
        Plan plan = getPlanById(planId);
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new Exception404("해당 멤버를 찾을 수 없습니다."));
        Participant participant = Participant.builder()
                .plan(plan)
                .member(member)
                .participantStatus(ParticipantStatus.PENDING)
                .build();
        Participant saved = participantRepository.save(participant);
        plan.getParticipants().add(saved);

        return saved;
    }

    // 참가자 삭제
    @Transactional
    public void removeParticipant(Long participantId) {
        if (!participantRepository.existsById(participantId)) {
            throw new Exception404("해당 참가자를 찾을 수 없습니다.");
        }
        participantRepository.deleteById(participantId);
    }

    // 참가자 상태 변경
    @Transactional
    public Participant changeParticipantStatus(Long participantId, ParticipantStatus status) {
        Participant pm = participantRepository.findById(participantId).orElseThrow(() -> new Exception404("참가자를 찾을 수 없습니다."));
        pm.setParticipantStatus(status);

        return participantRepository.save(pm);
    }

    // 참가자 조회
    public List<Participant> getParticipants(Long planId) {
        if (!planJpaRepository.existsById(planId)) {
            throw new Exception404("해당 플랜을 찾을 수 없습니다.");
        }
        return participantRepository.findByPlanId(planId);
    }

    // 출발 시간 기록
    @Transactional
    public Participant recordDeparture(Long participantId, LocalDateTime actualDeparture) {
        Participant pm = participantRepository.findById(participantId).orElseThrow(() -> new Exception404("참가자를 찾을 수 없습니다."));
        pm.setActualDepartureTime(actualDeparture != null ? actualDeparture : LocalDateTime.now());

        return participantRepository.save(pm);
    }

    // 도착 시간 기록
    @Transactional
    public Participant recordArrival(Long participantId, LocalDateTime actualArrival) {
        Participant pm = participantRepository.findById(participantId).orElseThrow(() -> new Exception404("참가자를 찾을 수 없습니다."));
        pm.setActualArrivalTime(actualArrival != null ? actualArrival : LocalDateTime.now());
        LocalDateTime planTime = pm.getPlan().getPlanDatetime();
        if (planTime == null) {
            throw new Exception400("플랜의 약속 시간이 설정되어 있지 않습니다.");
        }
        long minutesDiff = ChronoUnit.MINUTES.between(planTime, pm.getActualArrivalTime());
        pm.setTimeBurdenMinutes((int) minutesDiff);

        return participantRepository.save(pm);
    }

    // 예상 출발 시간 제안
    @Transactional
    public Participant suggestExpectedDeparture(Long participantId, Integer expectedTravelTimeMinutes) {
        Participant pm = participantRepository.findById(participantId).orElseThrow(() -> new Exception404("참가자를 찾을 수 없습니다."));
        pm.setExpectedTravelTimeMinutes(expectedTravelTimeMinutes);
        LocalDateTime planTime = pm.getPlan().getPlanDatetime();
        if (planTime == null) {
            throw new Exception400("플랜의 약속 시간이 설정되어 있지 않습니다.");
        }
        LocalDateTime expectedDeparture = planTime.minusMinutes(expectedTravelTimeMinutes != null ? expectedTravelTimeMinutes : 0);
        pm.setExpectedDepartureTime(expectedDeparture);

        return participantRepository.save(pm);
    }

    // 지각 벌금 계산
    public Long calculateLateFine(Long participantId) {
        Participant pm = participantRepository.findById(participantId).orElseThrow(() -> new Exception404("참가자를 찾을 수 없습니다."));
        Integer burden = pm.getTimeBurdenMinutes();
        if (burden != null && burden > 0) {
            Long fine = pm.getPlan().getLateFineAmount();
            return fine != null ? fine : 0L;
        }

        return 0L;
    }

    // 장소 확정
    @Transactional
    public Plan confirmPlace(Long planId, String placeName, Point location) {
        Plan plan = getPlanById(planId);
        plan.confirmPlace(placeName, location);

        return planJpaRepository.save(plan);
    }



    @Transactional(readOnly = true)
    public List<PlanResponse.CreatePlan> listPlansDto() {
        List<Plan> plans = planJpaRepository.findAllWithParticipants();
        return plans.stream().map(p -> PlanResponse.CreatePlan.of(p)).collect(Collectors.toList());
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
    public PlanResponse.CreatePlan updatePlanDto(Long planId, String title, LocalDateTime planDatetime, Status status, List<String> tags) {

        Plan plan = updatePlan(planId, title, planDatetime, status);

        // 태그 처리: null이면 변경 없음, 빈 리스트면 태그 제거
        if (tags != null) {
            List<String> normalized = tags.stream()
                    .filter(s -> Objects.nonNull(s))
                    .map(s -> s.trim())
                    .filter(s -> !s.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());

            // 기존 태그 삭제 후 새 태그 추가
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
    public PlanResponse.CreatePlan confirmPlaceDto(Long planId, String placeName, Point location) {
        Plan plan = confirmPlace(planId, placeName, location);
        Plan reloaded = planJpaRepository.findByIdWithParticipants(plan.getId()).orElse(plan);
        return PlanResponse.CreatePlan.of(reloaded);
    }

    @Transactional
    public ParticipantResponse addParticipantDto(Long planId, Long memberId) {
        Participant pm = addParticipant(planId, memberId);
        return ParticipantResponse.of(pm);
    }

    @Transactional
    public ParticipantResponse changeParticipantStatusDto(Long participantId, ParticipantStatus status) {
        Participant pm = changeParticipantStatus(participantId, status);
        return ParticipantResponse.of(pm);
    }

    @Transactional(readOnly = true)
    public List<ParticipantResponse> getParticipantsDto(Long planId) {
        List<Participant> list = getParticipants(planId);
        return list.stream().map(pm -> ParticipantResponse.of(pm)).collect(Collectors.toList());
    }

    @Transactional
    public ParticipantResponse recordDepartureDto(Long participantId, LocalDateTime actualDeparture) {
        Participant pm = recordDeparture(participantId, actualDeparture);
        return ParticipantResponse.of(pm);
    }

    @Transactional
    public ParticipantResponse recordArrivalDto(Long participantId, LocalDateTime actualArrival) {
        Participant pm = recordArrival(participantId, actualArrival);
        return ParticipantResponse.of(pm);
    }

    @Transactional
    public ParticipantResponse suggestExpectedDepartureDto(Long participantId, Integer expectedTravelTimeMinutes) {
        Participant pm = suggestExpectedDeparture(participantId, expectedTravelTimeMinutes);
        return ParticipantResponse.of(pm);
    }

}
