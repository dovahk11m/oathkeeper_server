package com.oath.domain.plan.domain;


import com.oath.domain.members.domain.Member;
import com.oath.domain.plan.ArrivalStatus;
import com.oath.domain.plan.ParticipantStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "plan_participants_tb")
@ToString(exclude = "plan")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 참가자 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan; // 약속

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // 참가 멤버

    @Enumerated(EnumType.STRING)
    @Column(name = "participant_status", nullable = false)
    private ParticipantStatus participantStatus; // 참가 상태 (PENDING/ACCEPTED/DECLINED)

    @Column(name = "transport_method")
    private String transportMethod; // 이동 수단 (WALK/BIKE/CAR/TRANSIT)

    @Column(name = "start_address")
    private String startAddress; // 출발지 주소

    @Column(name = "start_latitude")
    private Double startLatitude; // 출발지 위도

    @Column(name = "start_longitude")
    private Double startLongitude; // 출발지 경도

    @Column(name = "expected_travel_time_minutes")
    private Integer expectedTravelTimeMinutes; // 예상 이동 시간 (분)

    @Column(name = "expected_departure_time")
    private LocalDateTime expectedDepartureTime; // 예상 출발 시간

    @Column(name = "actual_departure_time")
    private LocalDateTime actualDepartureTime; // 실제 출발 시간

    @Column(name = "actual_arrival_time")
    private LocalDateTime actualArrivalTime; // 실제 도착 시간

    @Enumerated(EnumType.STRING)
    @Column(name = "arrival_status")
    private ArrivalStatus arrivalStatus; // 도착 상태 (ON_TIME/LATE/ABSENT)

    @Column(name = "arrival_offset_minutes")
    private Integer arrivalOffsetMinutes; // 도착 시간 차이 (분, 음수=일찍 도착)

    @Column(name = "time_burden_minutes")
    private Integer timeBurdenMinutes; // 시간 부담 (분)

    @Column(name = "departure_failure_reason")
    private String departureFailureReason; // 출발 실패 사유

    @Builder.Default
    @Column(name = "departed")
    private Boolean departed = false; // 출발 여부

    @Column(name = "last_live_ts")
    private LocalDateTime lastLiveTs; // 마지막 실시간 위치 수신 시각

    @Builder.Default
    @Column(name = "share_location_consent")
    private Boolean isShareLocation = true; // 위치 공유 동의 여부

    // 출발 처리
    public void markDeparted() {
        this.departed = true;
        this.actualDepartureTime = LocalDateTime.now();
    }

    // 도착 처리
    public void markArrived(ArrivalStatus status, Integer offsetMinutes) {
        this.arrivalStatus = status;
        this.arrivalOffsetMinutes = offsetMinutes;
    }

    // 실시간 위치 업데이트
    public void updateLastLiveLocation() {
        this.lastLiveTs = LocalDateTime.now();
    }

}
