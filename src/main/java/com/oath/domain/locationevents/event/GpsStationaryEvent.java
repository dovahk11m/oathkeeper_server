package com.oath.domain.locationevents.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class GpsStationaryEvent {
    private final Long planId;
    private final Long participantId;
    private final Long memberId;
    private final String username;
    private final Point lastKnownLocation; // 마지막으로 움직임이 감지된 위치
    private final LocalDateTime stationaryStartTime; // 정체 시작 시간
    private final long stationaryDurationMinutes; // 정체 지속 시간 (분)
}
