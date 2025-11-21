package com.oath.domain.locationevents.dto;

import lombok.Getter;
import lombok.NoArgsConstructor; // NoArgsConstructor 추가
import lombok.AllArgsConstructor; // AllArgsConstructor 추가

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor // 기본 생성자 추가
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자 추가
public class GpsStationaryNotificationDto {
    private final String eventType = "GPS_STATIONARY"; // 이벤트 타입 명시
    private Long planId;
    private Long participantId;
    private Long memberId;
    private String username;
    private Double lat; // x -> lat
    private Double lng; // y -> lng
    private LocalDateTime stationaryStartTime;
    private long stationaryDurationMinutes;
    private String message; // 클라이언트에게 보여줄 메시지
}
