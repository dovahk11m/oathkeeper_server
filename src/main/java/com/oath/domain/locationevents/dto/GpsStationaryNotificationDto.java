package com.oath.domain.locationevents.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class GpsStationaryNotificationDto {
    private final String eventType = "GPS_STATIONARY"; // 이벤트 타입 명시
    private final Long planId;
    private final Long participantId;
    private final Long memberId;
    private final String username;
    private final Point lastKnownLocation;
    private final LocalDateTime stationaryStartTime;
    private final long stationaryDurationMinutes;
    private final String message; // 클라이언트에게 보여줄 메시지
}
