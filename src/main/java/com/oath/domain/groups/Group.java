package com.oath.domain.groups;

import com.oath.domain.groups.groupDTO.GroupCreateRequest;
import com.oath.domain.plan.domain.Plan;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_tb") // 테이블 이름 컨벤션 통일
@Getter
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Builder
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // --- 그룹 스탯 필드 추가 ---
    @Builder.Default
    @ColumnDefault("0")
    @Column(nullable = false)
    private Integer totalPlansCompleted = 0;

    @Builder.Default
    @ColumnDefault("0")
    @Column(nullable = false)
    private Integer totalLateMinutes = 0;

    @Builder.Default
    @ColumnDefault("0")
    @Column(nullable = false)
    private Integer totalOnTimeArrivals = 0;

    @Builder.Default
    @ColumnDefault("0.0")
    @Column(nullable = false)
    private Double totalTravelDistance = 0.0;

    @Builder.Default
    @ColumnDefault("0")
    @Column(nullable = false)
    private Integer totalTravelTime = 0; // 분 단위

    // --- AI 요약 필드 ---
    @Lob
    private String summary;

    @Enumerated(EnumType.STRING)
    private SummaryStatus summaryStatus;

    private LocalDateTime summaryLastUpdatedAt;


    /**
     * DTO로부터 새로운 Group 엔티티를 생성하는 정적 팩토리 메서드입니다.
     * @param request 그룹 생성 요청 DTO
     * @return 생성된 Group 엔티티
     */
    public static Group from(GroupCreateRequest request) {
        return Group.builder()
                .name(request.getGroupName())
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * 완료된 Plan의 통계를 그룹 통계에 누적합니다.
     * @param completedPlan 완료된 Plan 엔티티
     */
    public void addPlanStatistics(Plan completedPlan) {
        this.totalPlansCompleted++;
        this.totalLateMinutes += completedPlan.getTotalLateMinutes();
        this.totalOnTimeArrivals += completedPlan.getTotalOnTimeArrivals();
        this.totalTravelDistance += completedPlan.getTotalTravelDistance();
        this.totalTravelTime += completedPlan.getTotalTravelTime();
    }

    /**
     * AI 요약 결과를 업데이트합니다.
     * @param summary AI가 생성한 요약 내용
     * @param status 요약 상태 (PENDING, COMPLETED, FAILED)
     */
    public void updateSummary(String summary, SummaryStatus status) {
        this.summary = summary;
        this.summaryStatus = status;
        this.summaryLastUpdatedAt = LocalDateTime.now();
    }
}
