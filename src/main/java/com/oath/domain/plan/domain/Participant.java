package com.oath.domain.plan.domain;


import com.oath.domain.members.domain.Member;
import com.oath.domain.plan.ArrivalStatus;
import com.oath.domain.plan.MovementStatus;
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
    @Builder.Default // PENDING을 기본값으로 설정
    private ParticipantStatus participantStatus = ParticipantStatus.PENDING; // 참가 상태 (PENDING/ACCEPTED/DECLINED)

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_status", nullable = false)
    @Builder.Default
    private MovementStatus movementStatus = MovementStatus.HOME; // 이동 상태 (HOME/DEPARTED/MOVING/STATIONARY/ARRIVED)

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
    private Boolean departed = false; // 출발 여부 (movementStatus로 대체 가능하지만, 기존 필드 유지)

    @Column(name = "last_live_ts")
    private LocalDateTime lastLiveTs; // 마지막 실시간 위치 수신 시각

    @Builder.Default
    @Column(name = "share_location_consent")
    private Boolean isShareLocation = true; // 위치 공유 동의 여부

    // 출발 처리
    public void markDeparted() {
        this.departed = true;
        this.actualDepartureTime = LocalDateTime.now();
        this.movementStatus = MovementStatus.DEPARTED; // 이동 상태 업데이트
    }

    // 도착 처리
    public void markArrived(ArrivalStatus status, Integer offsetMinutes) {
        this.arrivalStatus = status;
        this.arrivalOffsetMinutes = offsetMinutes;
        this.movementStatus = MovementStatus.ARRIVED; // 이동 상태 업데이트
    }

    // 실시간 위치 업데이트
    public void updateLastLiveLocation() {
        this.lastLiveTs = LocalDateTime.now();
    }

    // ParticipantStatus 변경 시 MovementStatus 초기화 또는 연동
    public void setParticipantStatus(ParticipantStatus participantStatus) {
        this.participantStatus = participantStatus;
        // ACCEPTED 상태가 되면 이동 상태를 HOME으로 초기화
        if (participantStatus == ParticipantStatus.ACCEPTED) {
            this.movementStatus = MovementStatus.HOME;
        }
        // REJECTED 상태가 되면 이동 상태를 ARRIVED로 간주 (더 이상 이동하지 않음)
        else if (participantStatus == ParticipantStatus.REJECTED) {
            this.movementStatus = MovementStatus.ARRIVED;
        }
    }

    public void setMovementStatus(MovementStatus movementStatus) {
        this.movementStatus = movementStatus;
    }
}
