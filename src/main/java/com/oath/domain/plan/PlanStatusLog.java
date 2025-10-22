package com.oath.domain.plan;


import com.oath.domain.members.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Table(name = "plan_status_logs_tb")
@Getter
@ToString(exclude = {"plan", "member"})
public class PlanStatusLog {

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
    @Column(name = "arrival_status", nullable = false)
    private ArrivalStatus state; // 출발/도착 상태

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt; // 상태 기록 시간

}
