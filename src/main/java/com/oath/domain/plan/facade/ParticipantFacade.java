package com.oath.domain.plan.facade;

import com.oath.domain.plan.domain.Participant;
import com.oath.domain.plan.ParticipantStatus;
import com.oath.domain.plan.service.PlanService;
import com.oath.domain.plan.request.ParticipantResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ParticipantFacade {

    private final PlanService planService;

    @Transactional
    public ParticipantResponse addParticipant(Long planId, Long memberId, Long requesterId) {
        Participant participant = planService.addParticipant(planId, memberId, requesterId);
        return ParticipantResponse.of(participant);
    }

    @Transactional
    public ParticipantResponse changeParticipantStatus(Long participantId, ParticipantStatus status, Long requesterId) {
        Participant participant = planService.changeParticipantStatus(participantId, status, requesterId);
        return ParticipantResponse.of(participant);
    }

    @Transactional(readOnly = true)
    public List<ParticipantResponse> getParticipants(Long planId) {
        List<Participant> participants = planService.getParticipants(planId);
        return participants.stream()
                .map(participant -> ParticipantResponse.of(participant))
                .collect(Collectors.toList());
    }

    @Transactional
    public ParticipantResponse recordDeparture(Long participantId, LocalDateTime actualDeparture, Long requesterId) {
        Participant participant = planService.recordDeparture(participantId, actualDeparture, requesterId);
        return ParticipantResponse.of(participant);
    }

    @Transactional
    public ParticipantResponse recordArrival(Long participantId, LocalDateTime actualArrival, Long requesterId) {
        Participant participant = planService.recordArrival(participantId, actualArrival, requesterId);
        return ParticipantResponse.of(participant);
    }

    @Transactional
    public ParticipantResponse suggestExpectedDeparture(Long participantId, Integer expectedTravelTimeMinutes, Long requesterId) {
        Participant participant = planService.suggestExpectedDeparture(participantId, expectedTravelTimeMinutes, requesterId);
        return ParticipantResponse.of(participant);
    }
}

