package com.oath.domain.recommend.plan;

import com.oath.domain.plan.Status;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "plan_embeddings", schema = "oath")
@Getter
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
}
