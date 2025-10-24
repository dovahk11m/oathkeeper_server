package com.oath.domain.latefines;

import com.oath.domain.plan.Participant;
import com.oath.domain.plan.Plan;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

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


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_participant_id", nullable = false)
    private Participant payerParticipant;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LateFineStatus status;

    private String paymentLink;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;
}
