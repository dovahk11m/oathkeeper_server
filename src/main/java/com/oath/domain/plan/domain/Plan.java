package com.oath.domain.plan.domain;

import com.oath.domain.members.domain.Member;
import com.oath.domain.place_tag_plan.plan_tag.PlanTag;
import com.oath.domain.plan.Status;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.geo.Point;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plan_tb")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_member_id", nullable = false)
    private Member creatorMember;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "plan_datetime", nullable = false)
    private LocalDateTime planDatetime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "place_name")
    private String placeName;

    @Column(name = "place_latitude")
    private Double placeLatitude;

    @Column(name = "place_longitude")
    private Double placeLongitude;

    @Column(name = "late_fine_amount")
    private Long lateFineAmount;

    // 참가자 목록
    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Participant> participants = new ArrayList<>();

    // 태그 목록 - 중간 테이블(PlanTag)을 통해 N:N 관계 설정
    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<PlanTag> planTags = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Builder
    public Plan(Member creatorMember, String title, LocalDateTime planDatetime, Status status, Long lateFineAmount) {
        this.creatorMember = creatorMember;
        this.title = title;
        this.planDatetime = planDatetime;
        this.status = status;
        this.lateFineAmount = lateFineAmount;
    }

    public void update(String title, LocalDateTime planDatetime, Status status) {
        if (title != null) this.title = title;
        if (planDatetime != null) this.planDatetime = planDatetime;
        if (status != null) this.status = status;
    }

    public void confirmPlace(String placeName, Point location) {
        this.placeName = placeName;
        if (location == null) {
            this.placeLatitude = null;
            this.placeLongitude = null;
        } else {
            //x=위도 y=경도
            this.placeLatitude = location.getY();
            this.placeLongitude = location.getX();
        }
    }

    // 약속 완료 처리
    public void complete() {
        this.status = Status.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    // 약속 완료 여부 확인
    public boolean isCompleted() {
        return this.status == Status.COMPLETED && this.completedAt != null;
    }

    // 계산/조회 편의용: DB의 위도/경도를 Spring Data Point로 변환
    @Transient
    public Point getPlaceLocation() {
        if (placeLatitude == null || placeLongitude == null) return null;
        return new Point(placeLongitude, placeLatitude);
    }

    public enum Polarity {
        POSITIVE, NEGATIVE
    }

    public enum Option {
        TOTAL, // 한명이 희생
        EQUAL  // 동등
    }

}
