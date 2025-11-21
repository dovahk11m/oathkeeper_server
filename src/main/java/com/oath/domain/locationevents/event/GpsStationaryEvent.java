package com.oath.domain.locationevents.event;

import lombok.Getter;
import lombok.AllArgsConstructor; // AllArgsConstructor 추가

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자 추가
public class GpsStationaryEvent {
    private final Long planId;
    private final Long participantId;
    private final Long memberId;
    private final String username;
    private final Double lat; // Point -> lat
    private final Double lng; // Point -> lng
    private final LocalDateTime stationaryStartTime; // 정체 시작 시간
    private final long stationaryDurationMinutes; // 정체 지속 시간 (분)
}
