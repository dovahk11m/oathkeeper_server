package com.oath.domain.alarms.strategies.alarmlocals;

import com.oath.domain.members.domain.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "alarms_tb")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlarmLocalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // 알림을 받는 사용자

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private boolean isRead;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}