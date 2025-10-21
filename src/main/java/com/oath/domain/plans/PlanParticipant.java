package com.oath.domain.plans;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "plan_participants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long planId; // FK plans.id

    @Column(nullable = false)
    private Long memberId; // FK members.id

    // TODO: 추후 POINT 타입으로 변경 필요
    @Column(nullable = false)
    private String startLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ArrivalStatus arrivalStatus;

    private Integer arrivalOffsetMinutes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
