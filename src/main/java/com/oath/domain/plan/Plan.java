package com.oath.domain.plan;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plan_tb")
@AllArgsConstructor
@Getter
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "title", nullable = false)
    private String title;


    @Column(name = "plan_datetime", nullable = false)
    private LocalDateTime meetingTime; // 약속 시간

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "place_name", nullable = false)
    private String placeName;


    @Column(name = "place_location")
    private String placeLocation; // 포인트 타입으로 한다고 했는데 일단 뭔지 몰라서 문자열로 둡니다

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlanMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlanStatusLog> statusLogs = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;


}
