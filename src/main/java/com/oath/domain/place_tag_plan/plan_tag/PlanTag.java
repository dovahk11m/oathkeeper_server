package com.oath.domain.place_tag_plan.plan_tag;

import com.oath.domain.place_tag_plan.tag.Tag;
import com.oath.domain.plan.domain.Plan;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "plan_tag_tb")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlanTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // N:1 관계 - Plan
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    // N:1 관계 - Tag
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;
    private LocalDateTime createdAt;

    @Builder
    public PlanTag(
            Plan plan,
            Tag tag,
            LocalDateTime createdAt
    ) {
        this.plan = plan;
        this.tag = tag;
        this.createdAt = createdAt;
    }
}
