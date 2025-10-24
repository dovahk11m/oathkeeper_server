package com.oath.domain.metrics.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "plan_member_metrics_tb",
        uniqueConstraints = @UniqueConstraint(columnNames = {"plan_id","member_id"}),
        indexes = {
                @Index(name="idx_metrics_plan", columnList="plan_id"),
                @Index(name="idx_metrics_member", columnList="member_id")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ParticipantMetrics {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="plan_id",   nullable = false)
    private Long planId;

    @Column(name="member_id", nullable = false)
    private Long memberId;

    /** 이동 거리(km, 소수점 2자리 정도로 저장) */
    @Column(name="distance_km")
    private Double distanceKm;

    /** 이동 시간(분) */
    @Column(name="travel_minutes")
    private Integer travelMinutes;

    /** 지각/대기/신뢰도/점수는 규칙 확정 뒤에 계산 */
    @Column(name="late_minutes")         private Integer lateMinutes;
    @Column(name="wait_minutes")         private Integer waitMinutes;
    @Column(name="depart_reliability")   private Integer departReliability;
    @Column(name="score")                private Integer score;

    @CreationTimestamp
    @Column(name="created_at", updatable = false)
    private LocalDateTime createdAt;
}

