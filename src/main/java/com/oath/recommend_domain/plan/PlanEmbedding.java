package com.oath.recommend_domain.plan;

import com.oath.common.util.DateUtil;
import com.oath.domain.plan.Status;
import com.oath.domain.plan.domain.Plan;
import com.oath.recommend_domain.plan.constants.DateMessage;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "plan_embeddings", schema = "oath")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlanEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long planId;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Column(columnDefinition = "oath.vector(768)")
    private float[] embedding;

    @Column(nullable = false)
    private LocalDateTime planDatetime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    private Double placeLatitude;
    private Double placeLongitude;

    @Builder
    public PlanEmbedding(Long planId, float[] embedding, LocalDateTime planDatetime, Status status, Double placeLatitude, Double placeLongitude) {
        this.planId = planId;
        this.embedding = embedding;
        this.planDatetime = planDatetime;
        this.status = status;
        this.placeLatitude = placeLatitude;
        this.placeLongitude = placeLongitude;
    }

    // 각 필드를 의미 있는 자연어로 바꾸는 메서드
    public static String getNaturalLanguage(PlanEmbedding planEmbedding, Plan plan) throws IllegalAccessException {

        String naturalLanguage = "";
        String[] splitDate = planEmbedding.getTime().split("/");
        naturalLanguage = naturalLanguage.concat(DateMessage.buildDateNaturalLanguage(planEmbedding.getWeekend(), splitDate[1], splitDate[2]));

        // 장소를 자연어로 처리
        naturalLanguage = naturalLanguage.concat(plan.getPlaceName() + "에서 ");

        List<String> memberNames = plan.getParticipants().stream()
                .map((participant) -> participant.getMember().getUsername())
                .toList();

        naturalLanguage = naturalLanguage.concat(plan.getParticipants().size() + "명과 만났다.");

        System.out.println(naturalLanguage);
        return naturalLanguage;
    }

    // 시간 포맷팅
    public String getTime() {
        return DateUtil.formatDate(this.planDatetime);
    }

    // 요일 포맷팅
    public String getWeekend() {
        return DateUtil.formatWeekendWithKorean(this.planDatetime);
    }
}
