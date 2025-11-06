package com.oath.recommend_domain.place;

import com.oath.domain.place_tag_plan.place.Place;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "place_embeddings", schema = "oath")
@Data
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

    // TODO 아직 장소 이름이나 태그가 없어서 이것도 추후에 해야함

    @Builder
    public PlaceEmbedding(float[] embedding, Long placeId, Double latitude, Double longitude) {
        this.embedding = embedding;
        this.placeId = placeId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // TODO 자연어 생성 메서드 구축
    public static String getNaturalLanguage(PlaceEmbedding placeEmbedding, Place place) {
        return "";
    }
}