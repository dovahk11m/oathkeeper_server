package com.oath.plan;


import com.oath.member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "plan_participants_tb")
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

    // 포인트 타입 뭔지 몰라서 일단 문자열
    @Column(name = "start_location")
    private String startLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "arrival_status", nullable = false)
    private ArrivalStatus arrivalStatus; // ON_TIME, LATE, ABSENT

    @Column(name = "arrival_offset_minutes")
    private Integer arrivalOffsetMinutes;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;


}
