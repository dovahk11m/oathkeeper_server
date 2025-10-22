package com.oath.domain.plan;

import com.oath.domain.members.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlanMember> participants = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

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

    public void confirmPlace(String placeName, Double latitude, Double longitude) {
        this.placeName = placeName;
        this.placeLatitude = latitude;
        this.placeLongitude = longitude;
    }

}
