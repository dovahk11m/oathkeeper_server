package com.oath.domain.locationevents.dto;

import lombok.Getter;
import lombok.AllArgsConstructor; // AllArgsConstructor 추가

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자 추가
public class ParticipantArrivedNotificationDto {
    private final String eventType = "PARTICIPANT_ARRIVED"; // 이벤트 타입 명시
    private Long planId;
    private Long participantId;
    private Long memberId;
    private String username;
    private Double lat; // arrivalLocation -> lat
    private Double lng; // arrivalLocation -> lng
    private LocalDateTime arrivalTime;
    private String message; // 클라이언트에게 보여줄 메시지
}
