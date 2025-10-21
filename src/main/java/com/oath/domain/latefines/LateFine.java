package com.oath.domain.latefines;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "late_fines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LateFine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long planId; // FK plans.id

    @Column(nullable = false)
    private Long payerParticipantId; // FK plan_participants.id

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LateFineStatus status;

    private String paymentLink;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;
}
