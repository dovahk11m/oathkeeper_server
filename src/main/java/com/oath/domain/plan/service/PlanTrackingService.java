package com.oath.domain.plan.service;

import com.oath.common.exception.Exception400;
import com.oath.common.exception.Exception403;
import com.oath.common.exception.Exception404;
import com.oath.domain.plan.ArrivalStatus;
import com.oath.domain.plan.ParticipantStatus;
import com.oath.domain.plan.Status;
import com.oath.domain.plan.domain.Participant;
import com.oath.domain.plan.domain.Plan;
import com.oath.domain.plan.event.AlarmType;
import com.oath.domain.plan.event.ArrivalEvent;
import com.oath.domain.plan.event.DepartureEvent;
import com.oath.domain.plan.event.LateEvent;
import com.oath.domain.plan.event.PlanCompletedEvent;
import com.oath.domain.plan.repository.ParticipantRepository;
import com.oath.domain.plan.repository.PlanJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional("h2TransactionManager")
public class PlanTrackingService {

    private final PlanJpaRepository planJpaRepository;
    private final ParticipantRepository participantRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PlanCoreService planCoreService; // PlanCoreService의 getPlanById, validatePlanCreator 사용

    // 출발 시간 기록 (본인만 가능)
    public Participant recordDeparture(
            Long participantId,
            LocalDateTime actualDeparture,
            Long requesterId
    ) {
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new Exception404("참가자를 찾을 수 없습니다."));

        // 본인 확인
        if (!participant.getMember()
                .getId()
                .equals(requesterId)) {
            throw new Exception403("본인의 출발 시간만 기록할 수 있습니다.");
        }

        participant.setActualDepartureTime(actualDeparture != null ? actualDeparture : LocalDateTime.now());
        participant.markDeparted();

        Participant saved = participantRepository.save(participant);

        // 출발 이벤트 발행 (10초 후 다른 참가자들에게 알림) -------- 10초 : 취소 가능 시간.
        Plan plan = participant.getPlan();
        // LazyInitializationException 해결을 위해 findOtherParticipantsWithMember 사용
        List<Participant> otherParticipants = participantRepository.findOtherParticipantsWithMember(
                plan.getId(),
                participantId
        );

        eventPublisher.publishEvent(
                new DepartureEvent(
                        plan,
                        saved,
                        otherParticipants,
                        AlarmType.REAL_TIME_DEPARTURE
                )
        );

        return saved;
    }

    // 도착 시간 기록 (본인만 가능)
    public Participant recordArrival(
            Long participantId,
            LocalDateTime actualArrival,
            Long requesterId
    ) {
        // 참가자 조회
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new Exception404("참가자를 찾을 수 없습니다."));

        // 본인 확인
        if (!participant.getMember()
                .getId()
                .equals(requesterId)) {
            throw new Exception403("본인의 도착 시간만 기록할 수 있습니다.");
        }

        participant.setActualArrivalTime(actualArrival != null ? actualArrival : LocalDateTime.now());

        LocalDateTime planTime = participant.getPlan()
                .getPlanDatetime();
        if (planTime == null) {
            throw new Exception400("플랜의 약속 시간이 설정되어 있지 않습니다.");
        }

        long minutesDiff = ChronoUnit.MINUTES.between(
                planTime,
                participant.getActualArrivalTime()
        );
        participant.setTimeBurdenMinutes((int) minutesDiff);

        // ArrivalStatus 설정
        ArrivalStatus arrivalStatus;
        if (minutesDiff > 0) {
            arrivalStatus = ArrivalStatus.LATE;
        } else {
            arrivalStatus = ArrivalStatus.ON_TIME;
        }

        participant.markArrived(
                arrivalStatus,
                (int) minutesDiff
        );

        Participant saved = participantRepository.save(participant);

        // 도착 이벤트 발행
        Plan plan = participant.getPlan();
        // LazyInitializationException 해결을 위해 findOtherParticipantsWithMember 사용
        List<Participant> otherParticipants = participantRepository.findOtherParticipantsWithMember(
                plan.getId(),
                participantId
        );

        eventPublisher.publishEvent(
                new ArrivalEvent(
                        plan,
                        saved,
                        otherParticipants,
                        AlarmType.ARRIVAL
                )
        );

        // 지각했다면 지각 이벤트도 발행
        if (arrivalStatus == ArrivalStatus.LATE) {
            eventPublisher.publishEvent(
                    new LateEvent(
                            plan,
                            saved,
                            otherParticipants,
                            (int) minutesDiff,
                            AlarmType.LATE
                    )
            );
        }

        // 🔸 약속 완료 여부 확인 및 이벤트 발행 로직 추가
        checkAndCompletePlan(plan.getId());

        return saved;
    }

    // 예상 출발 시간 제안 (본인만 가능)
    public Participant suggestExpectedDeparture(
            Long participantId,
            Integer expectedTravelTimeMinutes,
            Long requesterId
    ) {
        Participant pm = participantRepository.findById(participantId)
                .orElseThrow(() -> new Exception404("참가자를 찾을 수 없습니다."));

        // 본인 확인
        if (!pm.getMember()
                .getId()
                .equals(requesterId)) {
            throw new Exception403("본인의 예상 출발 시간만 설정할 수 있습니다.");
        }

        pm.setExpectedTravelTimeMinutes(expectedTravelTimeMinutes);
        LocalDateTime planTime = pm.getPlan()
                .getPlanDatetime();
        if (planTime == null) {
            throw new Exception400("플랜의 약속 시간이 설정되어 있지 않습니다.");
        }
        LocalDateTime expectedDeparture = planTime.minusMinutes(expectedTravelTimeMinutes != null ? expectedTravelTimeMinutes : 0);
        pm.setExpectedDepartureTime(expectedDeparture);

        return participantRepository.save(pm);
    }

    // 지각 벌금 계산 (본인 또는 생성자만 조회 가능)
    @Transactional(readOnly = true)
    public Long calculateLateFine(
            Long participantId,
            Long requesterId
    ) {
        Participant pm = participantRepository.findById(participantId)
                .orElseThrow(() -> new Exception404("참가자를 찾을 수 없습니다."));

        // 권한 검증: 본인 또는 플랜 생성자만 조회 가능
        boolean isOwner = pm.getMember()
                .getId()
                .equals(requesterId);
        boolean isCreator = pm.getPlan()
                .getCreatorMember()
                .getId()
                .equals(requesterId);

        if (!isOwner && !isCreator) {
            throw new Exception403("본인의 벌금만 조회할 수 있습니다.");
        }

        Integer burden = pm.getTimeBurdenMinutes();
        if (burden != null && burden > 0) {
            Long fine = pm.getPlan()
                    .getLateFineAmount();
            return fine != null ? fine : 0L;
        }

        return 0L;
    }

    // 🔸 약속 완료 여부 확인 및 이벤트 발행 (내부 메서드)
    private void checkAndCompletePlan(Long planId) {
        Plan plan = planCoreService.getPlanById(planId);
        List<Participant> participants = participantRepository.findByPlanId(planId);

        // 'ACCEPTED' 상태의 참가자들만 필터링
        List<Participant> acceptedParticipants = participants.stream()
                .filter(p -> p.getParticipantStatus() == ParticipantStatus.ACCEPTED)
                .collect(Collectors.toList());
        log.info("[checkAndCompletePlan] planId: {}, acceptedParticipants.size: {}", planId, acceptedParticipants.size());

        // 'ACCEPTED' 상태의 참가자가 있고, 그들 모두가 도착했는지 확인
        boolean allAcceptedParticipantsArrived = !acceptedParticipants.isEmpty() &&
                acceptedParticipants.stream().allMatch(p -> {
                    boolean arrived = p.getArrivalStatus() != null;
                    log.info("[checkAndCompletePlan] participantId: {}, arrivalStatus: {}, arrived: {}", p.getId(), p.getArrivalStatus(), arrived);
                    return arrived;
                });
        log.info("[checkAndCompletePlan] planId: {}, allAcceptedParticipantsArrived: {}", planId, allAcceptedParticipantsArrived);


        if (allAcceptedParticipantsArrived && plan.getStatus() != Status.COMPLETED) {
            log.info("[checkAndCompletePlan] Plan {} is being completed.", planId);
            plan.update(
                    plan.getTitle(),
                    plan.getPlanDatetime(),
                    Status.COMPLETED
            ); // 약속 상태 완료로 변경
            planJpaRepository.save(plan);
            eventPublisher.publishEvent(new PlanCompletedEvent(plan.getId())); // 이벤트 발행
        }
    }

    // 🔸 수동으로 플랜 완료 처리
    public Plan completePlanManually(
            Long planId,
            Long requesterId
    ) {
        // 1. 권한 검증: 플랜 생성자만 수동 완료 가능
        planCoreService.validatePlanCreator(
                planId,
                requesterId
        );

        // 2. 플랜 조회
        Plan plan = planCoreService.getPlanById(planId);

        // 3. 이미 완료된 상태인지 확인
        if (plan.getStatus() == Status.COMPLETED) {
            throw new Exception400("이미 완료된 약속입니다.");
        }

        // 4. 플랜 상태를 COMPLETED로 변경
        plan.update(
                plan.getTitle(),
                plan.getPlanDatetime(),
                Status.COMPLETED
        );

        Plan savedPlan = planJpaRepository.save(plan);

        // 5. 완료 이벤트 발행
        eventPublisher.publishEvent(new PlanCompletedEvent(savedPlan.getId()));

        return savedPlan;
    }

    // 🔸 [테스트용] 모든 참가자 도착 처리
    public void markAllArrivedForTest(
            Long planId,
            Long requesterId
    ) { // requesterId 추가
        planCoreService.validatePlanCreator(
                planId,
                requesterId
        ); // 권한 검증 추가
        Plan plan = planCoreService.getPlanById(planId);
        List<Participant> participants = participantRepository.findByPlanId(planId);
        LocalDateTime planTime = plan.getPlanDatetime();

        for (Participant participant : participants) {
            if (participant.getArrivalStatus() == null) { // 아직 도착하지 않은 참가자만 처리
                LocalDateTime arrivalTime;
                int minutesDiff;
                ArrivalStatus arrivalStatus;

                // user2 (ID=2)만 30분 지각 처리
                if (participant.getMember()
                        .getId()
                        .equals(2L)) {
                    arrivalTime = planTime.plusMinutes(30);
                    minutesDiff = 30;
                    arrivalStatus = ArrivalStatus.LATE;
                } else {
                    arrivalTime = planTime; // 정시 도착
                    minutesDiff = 0;
                    arrivalStatus = ArrivalStatus.ON_TIME;
                }

                participant.setActualArrivalTime(arrivalTime);
                participant.setTimeBurdenMinutes(minutesDiff);
                participant.markArrived(
                        arrivalStatus,
                        minutesDiff
                );
            }
        }
        participantRepository.saveAll(participants);

        // 모든 참가자 도착 처리 후, 약속 완료 로직 호출
        checkAndCompletePlan(planId);
    }
}
