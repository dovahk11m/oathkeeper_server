package com.oath.domain.plan.service;

import com.oath.common.exception.Exception400;
import com.oath.common.exception.Exception403;
import com.oath.common.exception.Exception404;
import com.oath.domain.members.domain.Member;
import com.oath.domain.members.repository.MemberRepository;
import com.oath.domain.plan.ArrivalStatus;
import com.oath.domain.plan.ParticipantStatus;
import com.oath.domain.plan.Status;
import com.oath.domain.plan.domain.Participant;
import com.oath.domain.plan.domain.Plan;
import com.oath.domain.plan.event.AlarmType;
import com.oath.domain.plan.event.ArrivalEvent;
import com.oath.domain.plan.event.DepartureEvent;
import com.oath.domain.plan.event.LateEvent;
import com.oath.domain.plan.repository.ParticipantRepository;
import com.oath.domain.plan.repository.PlanJpaRepository;
import com.oath.recommend_domain.plan.event_listener.PlanConfirmedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.geo.Point;
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
    private final ParticipantRepository participantRepository;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;


    // 플랜 조회
    public Plan getPlanById(Long planId) {
        return planJpaRepository.findById(planId).orElseThrow(() -> new Exception404("해당 플랜을 찾을 수 없습니다."));
    }

    // 플랜 접근 권한 검증 (생성자 또는 참가자만 접근 가능)
    @Transactional(readOnly = true)
    public void validatePlanAccess(Long planId, Long memberId) {
        Plan plan = planJpaRepository.findByIdWithParticipants(planId)
                .orElseThrow(() -> new Exception404("해당 플랜을 찾을 수 없습니다."));
        boolean isCreator = plan.getCreatorMember().getId().equals(memberId);
        boolean isParticipant = plan.getParticipants().stream()
                .anyMatch(p -> p.getMember().getId().equals(memberId));

        if (!isCreator && !isParticipant) {
            throw new Exception403("이 플랜에 접근할 권한이 없습니다.");
        }
    }

    // 플랜 생성자 권한 검증 (생성자만 가능)
    public void validatePlanCreator(Long planId, Long memberId) {
        Plan plan = getPlanById(planId);
        if (!plan.getCreatorMember().getId().equals(memberId)) {
            throw new Exception403("플랜 생성자만 수정/삭제할 수 있습니다.");
        }
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

    // 플랜 목록 조회 (본인이 생성하거나 참여한 플랜만)
    public List<Plan> listPlans(Long memberId) {
        return planJpaRepository.findAllByCreatorOrParticipant(memberId);
    }

    // 참가자 추가 (생성자만 가능)
    @Transactional
    public Participant addParticipant(Long planId, Long memberId, Long requesterId) {
        Plan plan = getPlanById(planId);

        // 권한 검증: 플랜 생성자만 참가자 추가 가능
        if (!plan.getCreatorMember().getId().equals(requesterId)) {
            throw new Exception403("플랜 생성자만 참가자를 추가할 수 있습니다.");
        }

        Member member = memberRepository.findById(memberId).orElseThrow(() -> new Exception404("해당 멤버를 찾을 수 없습니다."));
        Participant participant = Participant.builder()
                .plan(plan)
                .member(member)
                .participantStatus(ParticipantStatus.PENDING)
                .startAddress(member.getDefaultAddress())
                .startLatitude(member.getDefaultLat())
                .startLongitude(member.getDefaultLng())
                .build();
        Participant saved = participantRepository.save(participant);
        plan.getParticipants().add(saved);

        return saved;
    }

    // 참가자 삭제 (생성자 또는 본인만 가능)
    @Transactional
    public void removeParticipant(Long participantId, Long requesterId) {
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new Exception404("해당 참가자를 찾을 수 없습니다."));

        Plan plan = participant.getPlan();
        boolean isCreator = plan.getCreatorMember().getId().equals(requesterId);
        boolean isSelf = participant.getMember().getId().equals(requesterId);

        if (!isCreator && !isSelf) {
            throw new Exception403("플랜 생성자 또는 본인만 참가자를 삭제할 수 있습니다.");
        }

        participantRepository.deleteById(participantId);
    }

    // 참가자 상태 변경 (본인만 가능)
    @Transactional
    public Participant changeParticipantStatus(Long participantId, ParticipantStatus status, Long requesterId) {
        Participant participant = participantRepository.findById(participantId).orElseThrow(() -> new Exception404("참가자를 찾을 수 없습니다."));

        // 권한 검증: 참가자 본인만 상태 변경 가능
        if (!participant.getMember().getId().equals(requesterId)) {
            throw new Exception403("본인의 참가 상태만 변경할 수 있습니다.");
        }

        participant.setParticipantStatus(status);

        return participantRepository.save(participant);
    }

    // 참가자 조회
    public List<Participant> getParticipants(Long planId) {
        if (!planJpaRepository.existsById(planId)) {
            throw new Exception404("해당 플랜을 찾을 수 없습니다.");
        }
        return participantRepository.findByPlanId(planId);
    }

    // 출발 시간 기록 (본인만 가능)
    @Transactional
    public Participant recordDeparture(Long participantId, LocalDateTime actualDeparture, Long requesterId) {
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new Exception404("참가자를 찾을 수 없습니다."));

        // 본인 확인
        if (!participant.getMember().getId().equals(requesterId)) {
            throw new Exception403("본인의 출발 시간만 기록할 수 있습니다.");
        }

        participant.setActualDepartureTime(actualDeparture != null ? actualDeparture : LocalDateTime.now());
        participant.markDeparted();

        Participant saved = participantRepository.save(participant);

        // 출발 이벤트 발행 (10초 후 다른 참가자들에게 알림) -------- 10초 : 취소 가능 시간.
        Plan plan = participant.getPlan();
        List<Participant> otherParticipants = plan.getParticipants().stream()
                .filter(p -> !p.getId().equals(participantId))
                .collect(Collectors.toList());

        eventPublisher.publishEvent(
                new DepartureEvent(plan, saved, otherParticipants, AlarmType.REAL_TIME_DEPARTURE)
        );

        return saved;
    }

    // 도착 시간 기록 (본인만 가능)
    @Transactional
    public Participant recordArrival(Long participantId, LocalDateTime actualArrival, Long requesterId) {
        // 참가자 조회
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new Exception404("참가자를 찾을 수 없습니다."));

        // 본인 확인
        if (!participant.getMember().getId().equals(requesterId)) {
            throw new Exception403("본인의 도착 시간만 기록할 수 있습니다.");
        }

        participant.setActualArrivalTime(actualArrival != null ? actualArrival : LocalDateTime.now());

        LocalDateTime planTime = participant.getPlan().getPlanDatetime();
        if (planTime == null) {
            throw new Exception400("플랜의 약속 시간이 설정되어 있지 않습니다.");
        }

        long minutesDiff = ChronoUnit.MINUTES.between(planTime, participant.getActualArrivalTime());
        participant.setTimeBurdenMinutes((int) minutesDiff);

        // ArrivalStatus 설정
        ArrivalStatus arrivalStatus;
        if (minutesDiff > 0) {
            arrivalStatus = ArrivalStatus.LATE;
        } else {
            arrivalStatus = ArrivalStatus.ON_TIME;
        }

        participant.markArrived(arrivalStatus, (int) minutesDiff);

        Participant saved = participantRepository.save(participant);

        // 도착 이벤트 발행
        Plan plan = participant.getPlan();
        List<Participant> otherParticipants = plan.getParticipants().stream()
                .filter(p -> !p.getId().equals(participantId))
                .collect(Collectors.toList());

        eventPublisher.publishEvent(
                new ArrivalEvent(plan, saved, otherParticipants, AlarmType.ARRIVAL)
        );

        // 지각했다면 지각 이벤트도 발행
        if (arrivalStatus == ArrivalStatus.LATE) {
            eventPublisher.publishEvent(
                    new LateEvent(plan, saved, otherParticipants, (int) minutesDiff, AlarmType.LATE)
            );
        }

        return saved;
    }

    // 예상 출발 시간 제안 (본인만 가능)
    @Transactional
    public Participant suggestExpectedDeparture(Long participantId, Integer expectedTravelTimeMinutes, Long requesterId) {
        Participant pm = participantRepository.findById(participantId)
                .orElseThrow(() -> new Exception404("참가자를 찾을 수 없습니다."));

        // 본인 확인
        if (!pm.getMember().getId().equals(requesterId)) {
            throw new Exception403("본인의 예상 출발 시간만 설정할 수 있습니다.");
        }

        pm.setExpectedTravelTimeMinutes(expectedTravelTimeMinutes);
        LocalDateTime planTime = pm.getPlan().getPlanDatetime();
        if (planTime == null) {
            throw new Exception400("플랜의 약속 시간이 설정되어 있지 않습니다.");
        }
        LocalDateTime expectedDeparture = planTime.minusMinutes(expectedTravelTimeMinutes != null ? expectedTravelTimeMinutes : 0);
        pm.setExpectedDepartureTime(expectedDeparture);

        return participantRepository.save(pm);
    }

    // 지각 벌금 계산 (본인 또는 생성자만 조회 가능)
    public Long calculateLateFine(Long participantId, Long requesterId) {
        Participant pm = participantRepository.findById(participantId).orElseThrow(() -> new Exception404("참가자를 찾을 수 없습니다."));

        // 권한 검증: 본인 또는 플랜 생성자만 조회 가능
        boolean isOwner = pm.getMember().getId().equals(requesterId);
        boolean isCreator = pm.getPlan().getCreatorMember().getId().equals(requesterId);

        if (!isOwner && !isCreator) {
            throw new Exception403("본인의 벌금만 조회할 수 있습니다.");
        }

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

    @Transactional
    public Plan confirmFinalPlan(Long planId, Long requesterId) {

        validatePlanCreator(planId, requesterId);

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

        plan.update(plan.getTitle(), plan.getPlanDatetime(), Status.CONFIRMED);
        Plan savedPlan = planJpaRepository.save(plan);

        eventPublisher.publishEvent(new PlanConfirmedEvent(savedPlan.getId())); //

        return savedPlan;
    }
}
