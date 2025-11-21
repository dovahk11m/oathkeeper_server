package com.oath.domain.locationevents.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class ParticipantArrivedEvent {
    private final Long planId;
    private final Long participantId;
    private final Long memberId;
    private final String username;
    private final Point arrivalLocation;
    private final LocalDateTime arrivalTime;
}
