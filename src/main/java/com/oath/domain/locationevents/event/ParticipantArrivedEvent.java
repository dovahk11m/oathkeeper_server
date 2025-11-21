package com.oath.domain.locationevents.event;

import lombok.Getter;
import lombok.AllArgsConstructor; // AllArgsConstructor 추가

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자 추가
public class ParticipantArrivedEvent {
    private final Long planId;
    private final Long participantId;
    private final Long memberId;
    private final String username;
    private final Double lat; // arrivalLocation -> lat
    private final Double lng; // arrivalLocation -> lng
    private final LocalDateTime arrivalTime;
}
