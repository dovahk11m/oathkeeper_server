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

        // 참여 인원 자연어 처리
        for (String s : memberNames) {
            naturalLanguage = naturalLanguage.concat(s);

            if (!s.equals(memberNames.getLast()))
                naturalLanguage = naturalLanguage.concat(", ");
        }

        String lastMemberName = memberNames.getLast();

        if (hasFinalConsonant(lastMemberName.charAt(lastMemberName.length() - 1))) {
            naturalLanguage = naturalLanguage.concat("과 만났다.");
        } else {
            naturalLanguage = naturalLanguage.concat("와 만났다.");
        }

        System.out.println(naturalLanguage);
        return naturalLanguage;
    }

    private static boolean hasFinalConsonant(char ch) {
        if (ch < 0xAC00 || ch > 0xD7A3) {
            // 한글 음절이 아니면 false
            return false;
        }

        int baseCode = ch - 0xAC00;
        int finalConsonantIndex = baseCode % 28;

        return finalConsonantIndex != 0;
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
