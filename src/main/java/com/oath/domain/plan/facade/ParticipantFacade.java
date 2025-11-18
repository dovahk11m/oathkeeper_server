package com.oath.domain.plan.facade;

import com.oath.domain.plan.domain.Participant;
import com.oath.domain.plan.ParticipantStatus;
import com.oath.domain.plan.service.PlanParticipantService;
import com.oath.domain.plan.service.PlanTrackingService;
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

    private final PlanParticipantService planParticipantService;
    private final PlanTrackingService planTrackingService;

    @Transactional
    public ParticipantResponse addParticipant(Long planId, Long memberId, Long requesterId) {
        Participant participant = planParticipantService.addParticipant(planId, memberId, requesterId);
        return ParticipantResponse.of(participant);
    }

    @Transactional
    public void removeParticipant(Long participantId, Long requesterId) {
        planParticipantService.removeParticipant(participantId, requesterId);
    }

    @Transactional
    public ParticipantResponse changeParticipantStatus(Long participantId, ParticipantStatus status, Long requesterId) {
        Participant participant = planParticipantService.changeParticipantStatus(participantId, status, requesterId);
        return ParticipantResponse.of(participant);
    }

    @Transactional(readOnly = true)
    public List<ParticipantResponse> getParticipants(Long planId) {
        List<Participant> participants = planParticipantService.getParticipants(planId);
        return participants.stream()
                .map(ParticipantResponse::of)
                .collect(Collectors.toList());
    }

    @Transactional
    public ParticipantResponse recordDeparture(Long participantId, LocalDateTime actualDeparture, Long requesterId) {
        Participant participant = planTrackingService.recordDeparture(participantId, actualDeparture, requesterId);
        return ParticipantResponse.of(participant);
    }

    @Transactional
    public ParticipantResponse recordArrival(Long participantId, LocalDateTime actualArrival, Long requesterId) {
        Participant participant = planTrackingService.recordArrival(participantId, actualArrival, requesterId);
        return ParticipantResponse.of(participant);
    }

    @Transactional
    public ParticipantResponse suggestExpectedDeparture(Long participantId, Integer expectedTravelTimeMinutes, Long requesterId) {
        Participant participant = planTrackingService.suggestExpectedDeparture(participantId, expectedTravelTimeMinutes, requesterId);
        return ParticipantResponse.of(participant);
    }
}
