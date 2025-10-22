package com.oath.domain.plan;


import com.oath.domain.members.Member;
import jakarta.persistence.*;
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
public class PlanMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "participant_status", nullable = false)
    private ParticipantStatus participantStatus;

    @Column(name = "transport_method")
    private String transportMethod;

    @Column(name = "start_address")
    private String startAddress;

    @Column(name = "start_latitude")
    private Double startLatitude;

    @Column(name = "start_longitude")
    private Double startLongitude;

    @Column(name = "expected_travel_time_minutes")
    private Integer expectedTravelTimeMinutes;

    @Column(name = "expected_departure_time")
    private LocalDateTime expectedDepartureTime;

    @Column(name = "actual_departure_time")
    private LocalDateTime actualDepartureTime;

    @Column(name = "actual_arrival_time")
    private LocalDateTime actualArrivalTime;

    @Column(name = "time_burden_minutes")
    private Integer timeBurdenMinutes;

    @Column(name = "departure_failure_reason")
    private String departureFailureReason;

}
