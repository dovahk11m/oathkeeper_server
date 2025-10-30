package com.oath.recommend_domain.place;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "place_embeddings", schema = "oath")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long placeId;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Column(columnDefinition = "oath.vector(768)")
    private float[] embedding;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Builder
    public PlaceEmbedding(float[] embedding, Long placeId, Double latitude, Double longitude) {
        this.embedding = embedding;
        this.placeId = placeId;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}